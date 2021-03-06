package yourselvs.main;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import yourselvs.main.CommandProcessor.Cmd;
import yourselvs.utils.DateFormatter;
import yourselvs.utils.Messenger;

public class Plugin extends JavaPlugin 
{
	public static final String version = "0.1";
	public static final String pluginName = "Artifacts";
	public static final String prefix = "ARTIFACTS";
	
	public final static Object commandLock = new Object();
	
	private ArtifactHandler artifactHandler;
	private CommandProcessor commandProcessor;
	private DateFormatter formatter;
	private Messenger messenger;
	
	private String normalPrefix = "[" + ChatColor.DARK_GREEN + ChatColor.BOLD + prefix + ChatColor.RESET + "] ";
	private String linkPrefix = ChatColor.AQUA + "[" + ChatColor.DARK_GREEN + ChatColor.BOLD + prefix + ChatColor.RESET + ChatColor.AQUA + "]" + ChatColor.RESET + " ";
	
	public String getPluginName() {return pluginName;}
	
	public Messenger getMessenger() {return messenger;}
	public DateFormatter getFormatter() {return formatter;}
	public CommandProcessor getCommandProcessor() {return commandProcessor;}
	public ArtifactHandler getArtifactHandler() {return artifactHandler;}
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
    	
    	formatter = new DateFormatter();
    	messenger = new Messenger(this, normalPrefix, linkPrefix, ChatColor.YELLOW);

    	commandProcessor = new CommandProcessor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Thread commandThread = new Thread(new Runnable() {
	        public void run(){
	        	synchronized(commandLock) {
	        		commandProcessor.parseCommand(new Cmd(sender, command, label, args));
	        	}
	        }
		});
		
		commandThread.setName(pluginName + " Command Processor");
		commandThread.start();
		
		return true;
	}
}