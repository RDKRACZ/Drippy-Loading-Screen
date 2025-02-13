package de.keksuccino.drippyloadingscreen.customization.placeholdervalues;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry;
import de.keksuccino.drippyloadingscreen.api.PlaceholderTextValueRegistry.PlaceholderValue;
import de.keksuccino.konkrete.input.StringUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;

public class PlaceholderTextValueHelper {
	
	public static String convertFromRaw(String in) {
		try {

			MinecraftClient mc = MinecraftClient.getInstance();

			if (mc == null) {
				return in;
			}

			//Convert &-formatcodes to real ones
			in = StringUtils.convertFormatCodes(in, "&", "§");

			//Only for internal use
			in = in.replace("%guiwidth%", "" + MinecraftClient.getInstance().getWindow().getScaledWidth());
			in = in.replace("%guiheight%", "" + MinecraftClient.getInstance().getWindow().getScaledHeight());
			//-------------

			//Replace player name and uuid placeholders
			in = in.replace("%playername%", mc.getSession().getUsername());
			in = in.replace("%playeruuid%", mc.getSession().getUuid());

			//Replace mc version placeholder
			in = in.replace("%mcversion%", SharedConstants.getGameVersion().getReleaseTarget());

			//Replace mod version placeholder
			in = replaceModVersionPlaceholder(in);

			//Replace loaded mods placeholder
			in = in.replace("%loadedmods%", "" + getLoadedMods());

			//Replace total mods placeholder
			in = in.replace("%totalmods%", "" + getTotalMods());

			if (in.contains("%realtime")) {

				Calendar c = Calendar.getInstance();

				in = in.replace("%realtimeyear%", "" + c.get(Calendar.YEAR));

				in = in.replace("%realtimemonth%", formatToFancyDateTime(c.get(Calendar.MONTH) + 1));

				in = in.replace("%realtimeday%", formatToFancyDateTime(c.get(Calendar.DAY_OF_MONTH)));

				in = in.replace("%realtimehour%", formatToFancyDateTime(c.get(Calendar.HOUR_OF_DAY)));

				in = in.replace("%realtimeminute%", formatToFancyDateTime(c.get(Calendar.MINUTE)));

				in = in.replace("%realtimesecond%", formatToFancyDateTime(c.get(Calendar.SECOND)));

			}

			if (in.contains("%fps%")) {
				in = in.replace("%fps%", mc.fpsDebugString.split("[ ]", 2)[0]);
			}

			if (in.contains("ram%")) {
				long i = Runtime.getRuntime().maxMemory();
				long j = Runtime.getRuntime().totalMemory();
				long k = Runtime.getRuntime().freeMemory();
				long l = j - k;

				in = in.replace("%percentram%", (l * 100L / i) + "%");

				in = in.replace("%usedram%", "" + bytesToMb(l));

				in = in.replace("%maxram%", "" + bytesToMb(i));
			}

			//Apply all custom values
			for (PlaceholderValue v : PlaceholderTextValueRegistry.getInstance().getValuesAsList()) {
				in = in.replace(v.getPlaceholder(), v.get());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return in;
	}
	
	public static boolean containsPlaceholderValues(String in) {
		String s = convertFromRaw(in);
		return !s.equals(in);
	}

	private static String replaceModVersionPlaceholder(String in) {
		try {
			if (in.contains("%version:")) {
				List<String> l = new ArrayList<String>();
				int index = -1;
				int i = 0;
				while (i < in.length()) {
					String s = "" + in.charAt(i);
					if (s.equals("%")) {
						if (index == -1) {
							index = i;
						} else {
							String sub = in.substring(index, i+1);
							if (sub.startsWith("%version:") && sub.endsWith("%")) {
								l.add(sub);
							}
							index = -1;
						}
					}
					i++;
				}
				for (String s : l) {
					if (s.contains(":")) {
						String blank = s.substring(1, s.length()-1);
						String mod = blank.split(":", 2)[1];
						if (FabricLoader.getInstance().isModLoaded(mod)) {
							Optional<ModContainer> o = FabricLoader.getInstance().getModContainer(mod);
							if (o.isPresent()) {
								ModContainer c = o.get();
								String version = c.getMetadata().getVersion().getFriendlyString();
								in = in.replace(s, version);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}

	private static int getTotalMods() {
		File modDir = new File("mods");
		if (modDir.exists()) {
			int i = 0;
			File[] modList = modDir.listFiles();
			if (modList != null) {
				for (File f : modList) {
					if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
						i++;
					}
				}
			}
			return i+2;
		}
		return -1;
	}

	private static int getLoadedMods() {
		try {
			return FabricLoader.getInstance().getAllMods().size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static String formatToFancyDateTime(int in) {
		String s = "" + in;
		if (s.length() < 2) {
			s = "0" + s;
		}
		return s;
	}
	
	private static long bytesToMb(long bytes) {
		return bytes / 1024L / 1024L;
	}

}
