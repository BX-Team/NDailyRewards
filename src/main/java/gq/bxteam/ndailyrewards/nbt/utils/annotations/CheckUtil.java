package gq.bxteam.ndailyrewards.nbt.utils.annotations;

import java.lang.reflect.Method;

import gq.bxteam.ndailyrewards.nbt.NbtApiException;
import gq.bxteam.ndailyrewards.nbt.utils.MinecraftVersion;

public class CheckUtil {

	public static boolean isAvaliable(Method method) {
		if(MinecraftVersion.getVersion().getVersionId() < method.getAnnotation(AvailableSince.class).version().getVersionId())
			throw new NbtApiException("The Method '" + method.getName() + "' is only avaliable for the Versions " + method.getAnnotation(AvailableSince.class).version() + "+, but still got called!");
		return true;
	}
	
}