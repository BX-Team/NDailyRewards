package gq.bxteam.ndailyrewards.nbt;

import com.mojang.authlib.GameProfile;

import gq.bxteam.ndailyrewards.nbt.utils.nmsmappings.ObjectCreator;
import gq.bxteam.ndailyrewards.nbt.utils.nmsmappings.ReflectionMethod;

@Deprecated // authlib 1.5.25 have CVE-2021-29425 and log4j2 vulnerability. This comment will be deleted when 1.20 will be released.
public class NBTGameProfile {

    /**
     * Convert a GameProfile to NBT. The NBT then can be modified or be stored
     * 
     * @param profile
     * @return A NBTContainer with all the GameProfile data
     */
    public static NBTCompound toNBT(GameProfile profile) {
        return new NBTContainer(ReflectionMethod.GAMEPROFILE_SERIALIZE.run(null,
                ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(), profile));
    }

    /**
     * Reconstructs a GameProfile from a NBTCompound
     * 
     * @param compound Has to contain GameProfile data
     * @return The reconstructed GameProfile
     */
    public static GameProfile fromNBT(NBTCompound compound) {
        return (GameProfile) ReflectionMethod.GAMEPROFILE_DESERIALIZE.run(null,
                NBTReflectionUtil.gettoCompount(compound.getCompound(), compound));
    }

}
