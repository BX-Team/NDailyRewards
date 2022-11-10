package gq.bxteam.ndailyrewards.nbt.utils.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import gq.bxteam.ndailyrewards.nbt.utils.MinecraftVersion;

@Retention(RUNTIME)
@Target({ METHOD })
public @interface AvailableSince {

	MinecraftVersion version();

}