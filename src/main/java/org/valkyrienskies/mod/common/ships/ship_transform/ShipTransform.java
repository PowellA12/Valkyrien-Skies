package org.valkyrienskies.mod.common.ships.ship_transform;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienNBTUtils;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable wrapper around the rotation matrices used by ships. The immutability is extremely
 * important to enforce for preventing multi-threaded access issues. All access to the internal
 * arrays is blocked to guarantee nothing goes wrong.
 * <p>
 * Used to transform vectors between the global coordinate system, and the subspace (ship)
 * coordinate system. TODO: Move this to VS API.
 *
 * @author thebest108
 */
@Immutable
@With
@Value
@Log4j2
@Builder(toBuilder = true)
@NonFinal
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ShipTransform {

    /**
     * A transformation matrix used to convert 'subspace' coordinates into 'global' coordinates.
     */
    @JsonDeserialize(as = Matrix4d.class)
    private final Matrix4dc subspaceToGlobal;
    /**
     * A transformation matrix used to convert 'global' coordinates into 'subspace coordinates'
     */
    @JsonDeserialize(as = Matrix4d.class)
    private final Matrix4dc globalToSubspace;

    private final double posX, posY, posZ;
    private final Vector3dc centerCoord;

    public ShipTransform(Vector3dc position, Vector3dc centerCoord) {
        this(position.x(), position.y(), position.z(), new Quaterniond(), centerCoord);
    }

    public ShipTransform(Vector3dc position, Quaterniondc rotation, Vector3dc centerCoord) {
        this(position.x(), position.y(), position.z(), rotation, centerCoord);
    }

    public ShipTransform(double posX, double posY, double posZ, Quaterniondc rotation, Vector3dc centerCoord) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.centerCoord = centerCoord;

        this.subspaceToGlobal = new Matrix4d()
            // Finally we translate the coordinates to where they are in the world.
            .translate(posX, posY, posZ)
            // Then we rotate about the coordinate origin based on the pitch/yaw/roll.
            .rotate(rotation)
            // First translate the block coordinates to coordinates where center of mass is <0,0,0>
            // E.g., move the coordinate origin to <0,0,0>
            .translate(-centerCoord.x(), -centerCoord.y(), -centerCoord.z());

        this.globalToSubspace = subspaceToGlobal.invert(new Matrix4d());
    }

    public BlockPos getShipPositionBlockPos() {
        return new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
    }

    public Vec3d getShipPositionVec3d() {
        return new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ());
    }

    public static Matrix4d createTransform(ShipTransform prev, ShipTransform current) {
        return current.subspaceToGlobal.mul(prev.globalToSubspace, new Matrix4d());
    }

    public void transformPosition(Vector3d position, TransformType transformType) {
        getTransformMatrix(transformType).transformPosition(position);
    }

    public void transformDirection(Vector3d direction, TransformType transformType) {
        getTransformMatrix(transformType).transformDirection(direction);
    }

    public Vec3d transform(Vec3d vec3d, TransformType transformType) {
        Vector3d vec3dAsVector = JOML.convert(vec3d);
        transformPosition(vec3dAsVector, transformType);
        return JOML.toMinecraft(vec3dAsVector);
    }

    public Vec3d rotate(Vec3d vec3d, TransformType transformType) {
        Vector3d vec3dAsVector = new Vector3d(vec3d.x, vec3d.y, vec3d.z);
        transformDirection(vec3dAsVector, transformType);
        return new Vec3d(vec3dAsVector.x, vec3dAsVector.y, vec3dAsVector.z);
    }

    public BlockPos transform(BlockPos pos, TransformType transformType) {
        Vector3d blockPosAsVector = new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
        transformPosition(blockPosAsVector, transformType);
        return new BlockPos(blockPosAsVector.x - .5D, blockPosAsVector.y - .5D,
            blockPosAsVector.z - .5D);
    }

    public Quaterniond rotationQuaternion(TransformType transformType) {
        return getTransformMatrix(transformType).getNormalizedRotation(new Quaterniond());
    }

    public void writeToNBT(NBTTagCompound compound, String name) {
        compound.setByteArray(name,
            ValkyrienNBTUtils.toByteArray(subspaceToGlobal.get(new double[16])));
    }

    /**
     * Creates a standard 3x3 rotation matrix for this transform and the given transform type.
     */
    public Matrix3dc createRotationMatrix(TransformType transformType) {
        return getTransformMatrix(transformType).get3x3(new Matrix3d());
    }

    /**
     * Returns the same matrix this object has (not a copy). For that reason please <h1>DO NOT CAST
     * THIS</h1> to Matrix4d. Doing so would violate the contract that the internal transform never
     * changes, so DO NOT DO IT! You would be worse than Thanos! You wouldn't break half the mod,
     * you would break EVERYTHING. Your computer would explode, your house would burn down, your dog
     * will die, you'll be exiled from your home country, and your parents will disown you. Even if
     * you so much as think about casting this back to a Matrix4d you'll likely get struck by an
     * asteroid. You've been warned.
     *
     * @deprecated use {@link #getSubspaceToGlobal()} and {@link #getGlobalToSubspace()} instead
     */
    @Deprecated
    public Matrix4dc getTransformMatrix(TransformType transformType) {
        switch (transformType) {
            case SUBSPACE_TO_GLOBAL:
                return subspaceToGlobal;
            case GLOBAL_TO_SUBSPACE:
                return globalToSubspace;
            default:
                throw new IllegalArgumentException(
                    "Unexpected TransformType Enum: " + transformType);
        }
    }

    public void transform(final Entity player, final TransformType globalToSubspace, final boolean transformEntityBoundingBox) {
        ValkyrienUtils.transformEntity(getTransformMatrix(globalToSubspace), player, transformEntityBoundingBox);
    }
}
