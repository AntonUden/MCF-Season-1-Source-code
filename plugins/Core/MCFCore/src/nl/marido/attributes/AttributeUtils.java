package nl.marido.attributes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class AttributeUtils {

	private static String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	private static Class<?> craftitemstackclass, itemstackclass, nbttagcompoundclass, nbttaglistclass;
	private static Class<?> nbttagstringclass, nbttagintclass, nbtbaseclass;
	private static Constructor<?> nbttagstringconstructor, nbttagintconstructor;
	private static Method nbttagcompoundsetmethod, nbttaglistaddmethod, itemstacksettagmethod;

	static {
		try {
			craftitemstackclass = getObcClass("inventory.CraftItemStack");
			itemstackclass = getNmsClass("ItemStack");
			nbttagcompoundclass = getNmsClass("NBTTagCompound");
			nbttaglistclass = getNmsClass("NBTTagList");
			nbttagstringclass = getNmsClass("NBTTagString");
			nbttagintclass = getNmsClass("NBTTagInt");
			nbtbaseclass = getNmsClass("NBTBase");
			nbttagintconstructor = nbttagintclass.getConstructor(int.class);
			nbttagstringconstructor = nbttagstringclass.getConstructor(String.class);
			nbttagcompoundsetmethod = nbttagcompoundclass.getMethod("set", String.class, nbtbaseclass);
			nbttaglistaddmethod = nbttaglistclass.getMethod("add", nbtbaseclass);
			itemstacksettagmethod = itemstackclass.getMethod("setTag", nbttagcompoundclass);
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	public static ItemStack addAttribute(ItemStack item, String attribute, int amount) {
		try {
			Object nmsitem = craftitemstackclass.getMethod("asNMSCopy", ItemStack.class).invoke(craftitemstackclass, item);
			Object compound = itemstackclass.getMethod("getTag").invoke(itemstackclass.cast(nmsitem));
			if (compound == null) {
				compound = nbttagcompoundclass.newInstance();
				itemstacksettagmethod.invoke(nmsitem, nbttagcompoundclass.cast(compound));
			}
			Object modifiers = nbttaglistclass.newInstance();
			Object details = nbttagcompoundclass.newInstance();
			nbttagcompoundsetmethod.invoke(details, "AttributeName", nbttagstringconstructor.newInstance(attribute));
			nbttagcompoundsetmethod.invoke(details, "Name", nbttagstringconstructor.newInstance(attribute));
			nbttagcompoundsetmethod.invoke(details, "Amount", nbttagintconstructor.newInstance(amount));
			nbttagcompoundsetmethod.invoke(details, "Operation", nbttagintconstructor.newInstance(0));
			nbttagcompoundsetmethod.invoke(details, "UUIDLeast", nbttagintconstructor.newInstance(894654));
			nbttagcompoundsetmethod.invoke(details, "UUIDMost", nbttagintconstructor.newInstance(2872));
			nbttaglistaddmethod.invoke(modifiers, details);
			nbttagcompoundsetmethod.invoke(compound, "AttributeModifiers", nbttaglistclass.cast(modifiers));
			itemstacksettagmethod.invoke(nmsitem, compound);
			item = (ItemStack) craftitemstackclass.getMethod("asBukkitCopy", itemstackclass).invoke(craftitemstackclass, itemstackclass.cast(nmsitem));
		} catch (Exception error) {
			error.printStackTrace();
		}
		return item;
	}

	public static String getAttributes(ItemStack item) {
		try {
			Object nmsitem = craftitemstackclass.getMethod("asNMSCopy", ItemStack.class).invoke(craftitemstackclass, item);
			Object compound = itemstackclass.getMethod("getTag").invoke(itemstackclass.cast(nmsitem));
			if (compound == null) {
				compound = nbttagcompoundclass.newInstance();
				itemstacksettagmethod.invoke(nmsitem, nbttagcompoundclass.cast(compound));
			}
			Object details = nbttagcompoundclass.newInstance();
			return details.toString();
		} catch (Exception error) {
			error.printStackTrace();
		}
		return null;
	}

	public static Class<?> getNmsClass(String classname) {
		String fullname = "net.minecraft.server." + version + classname;
		Class<?> realclass = null;
		try {
			realclass = Class.forName(fullname);
		} catch (Exception error) {
			error.printStackTrace();
		}
		return realclass;
	}

	public static Class<?> getObcClass(String classname) {
		String fullname = "org.bukkit.craftbukkit." + version + classname;
		Class<?> realclass = null;
		try {
			realclass = Class.forName(fullname);
		} catch (Exception error) {
			error.printStackTrace();
		}
		return realclass;
	}

}