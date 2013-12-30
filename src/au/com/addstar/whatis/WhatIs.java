package au.com.addstar.whatis;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.commands.CommandFinder;

public class WhatIs extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		CommandFinder.init();
		
		CommandDispatcher whatis = new CommandDispatcher("whatis", "");
		whatis.registerCommand(new EventViewCommand());
		whatis.registerCommand(new CommandCommand());
		File reportDir = new File(getDataFolder(), "reports");
		reportDir.mkdirs();
		whatis.registerCommand(new ReportCommand(reportDir));
		
		getCommand("whatis").setExecutor(whatis);
		getCommand("whatis").setTabCompleter(whatis);
		
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				getLogger().info("Building event name map");
				EventHelper.buildEventMap();
			}
		});
	}
	
	public static File getPluginSource(Plugin plugin)
	{
		if(plugin instanceof JavaPlugin)
		{
			try
			{
				Field field = JavaPlugin.class.getDeclaredField("file");
				field.setAccessible(true);
				return (File)field.get(plugin);
			}
			catch(Exception e)
			{
				// Wont happen
				return null;
			}
		}
		
		return null;
	}
	
	public static List<BukkitTask> getPluginTasks(Plugin plugin)
	{
		ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		
		for(BukkitTask pending : Bukkit.getScheduler().getPendingTasks())
		{
			if(pending.getOwner() == plugin)
				tasks.add(pending);
		}
		
		return tasks;
	}
	
	private static Method mGetTaskClass = null;

	public static String getTaskSource(BukkitTask task)
	{
		try
		{
			if(mGetTaskClass == null)
			{
				Class<?> clazz = task.getClass();
				while(clazz != null && !clazz.getSimpleName().equals("CraftTask"))
					clazz = clazz.getSuperclass();
				
				if(clazz == null)
					return null;
				
				mGetTaskClass = clazz.getDeclaredMethod("getTaskClass");
				mGetTaskClass.setAccessible(true);
			}
			
			@SuppressWarnings( "unchecked" )
			Class<? extends Runnable> clazz = (Class<? extends Runnable>)mGetTaskClass.invoke(task);
			
			return clazz.getName();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
