package yourselvs.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class CommandProcessor {

	public static class Cmd {
		public CommandSender sender;
		public Command cmd;
		public String label;
		public String[] args;
		
		public Cmd(CommandSender sender, Command cmd, String label, String[] args){
			this.sender = sender;
			this.cmd = cmd;
			this.label = label;
			this.args = args;
		}
	}
	
	private Plugin plugin;

	public CommandProcessor(Plugin plugin) {
		this.plugin = plugin;
	}

	public void parseCommand(Cmd cmd) {
		switch(cmd.label) {
		case "redeem":
			parseRedeem(cmd);
			break;
		case "artifact":
			parseArtifact(cmd);
			break;		
		}
	}
	
	private void parseRedeem(Cmd cmd) {
		Player player;
		int numRedeemed = 0, amountRedeemed = 0;
		ItemStack air = new ItemStack(Material.AIR);
		
		// If user doesn't have permission, send error message
		if(!cmd.sender.hasPermission("artifact.redeem")) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You don't have permission to do this.");
			return;
		}

		// If user isn't a player, send error message
		if(!(cmd.sender instanceof Player)) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You must be a player to do this.");
			return;
		}
		
		// Get player
		player = (Player) cmd.sender;
		
		// Loop through all stacks of artifacts in player inventory
		while(player.getInventory().contains(plugin.getArtifactHandler().getArtifact())) {
			// Get location and of first found artifact
			int first = player.getInventory().first(plugin.getArtifactHandler().getArtifact());
			
			// Get ItemStack of said artifact
			ItemStack artifacts = player.getInventory().getItem(first);
			
			// Loop through all artifacts in stack
			for(int i = 0; i < artifacts.getAmount(); i++) {
				// Calculate artifact value for each one in stack
				amountRedeemed += plugin.getArtifactHandler().redeemArtifact();
			}
			
			// Add number of artifacts to total amount
			numRedeemed += artifacts.getAmount();
			
			// Replace artifact with air
			player.getInventory().setItem(first, air);
		}
		
		// If no artifacts were found, send error message
		if(numRedeemed <= 0) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You do not have any " + 
					ChatColor.YELLOW + "Artifacts" + ChatColor.RESET + " to redeem.");
		}
		else {
			// Make artifact string
			String artifacts = ChatColor.YELLOW + "" + numRedeemed + " Artifact";
			
			// Add plural
			if(numRedeemed > 1) {
				artifacts += "s";
			}
			
			// Send message to player with how much they earned from artifacts
			plugin.getMessenger().sendMessage(cmd.sender, artifacts + ChatColor.RESET + 
					" redeemed for a total of $" + amountRedeemed);
		}
	}
	
	private void parseArtifact(Cmd cmd) {
		if(cmd.args.length < 1) {
			// If param isn't included, send help message
			parseArtifactHelp(cmd);
			return;
		}
		
		// Determine sub-command to execute
		switch(cmd.args[0]) {
		case "help":
			parseArtifactHelp(cmd);
			break;
		case "set":
			parseArtifactSet(cmd);
			break;
		case "time":
			parseArtifactTime(cmd);
			break;
		case "start":
			parseArtifactStart(cmd);
			break;
		case "stop":
			parseArtifactStop(cmd);
			break;
		case "state":
			parseArtifactState(cmd);
			break;
		default:
			parseArtifactError(cmd);
		}
	}
	
	
	private void parseArtifactHelp(Cmd cmd) {
			
	}
	
	private void parseArtifactSet(Cmd cmd) {
		ItemStack handStack;
		Player player;
		
		// If user doesn't have permission, send error message
		if(!cmd.sender.hasPermission("artifact.set")) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You don't have permission to do this.");
			return;
		}

		// If user isn't a player, send error message
		if(!(cmd.sender instanceof Player)) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You must be a player to do this.");
			return;
		}
		
		// Abstract player
		player = (Player) cmd.sender;
		
		// Get player main hand
		handStack = player.getInventory().getItemInMainHand();

		// If holding nothing in main hand, send error message
		if(handStack.getType() == Material.AIR) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You aren't holding anything in your main hand.");
			return;
		}
		
		// Set the amount in an artifact stack to 1, as artifacts are given one at a time
		handStack.setAmount(1);

		// Set the new artifact
		plugin.getArtifactHandler().setArtifact(handStack);
		
		// Send user message saying artifact has been updated
		plugin.getMessenger().sendMessage(cmd.sender, "The artifact has been set to the item in your main hand.");
	}
	
	private void parseArtifactTime(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!cmd.sender.hasPermission("artifact.time")) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You don't have permission to do this.");
		}
		// If no params are included, send user the current time
		else if(cmd.args.length == 1) { 
			int time = plugin.getArtifactHandler().getSeconds();
			
			plugin.getMessenger().sendMessage(cmd.sender, "Artifacts drop every " + ChatColor.YELLOW + time + ChatColor.RESET + " seconds.");
		}
		// If time param is included, but not an integer, send error message
		else if(!isParsableInteger(cmd.args[1])) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "The time must be an integer in seconds.");
		}
		else {
			// Parse time param
			int time = Integer.parseInt(cmd.args[1]);
			
			// Set new timer
			plugin.getArtifactHandler().setSeconds(time);
			
			// Send user message notifying them of the change
			plugin.getMessenger().sendMessage(cmd.sender, "Artifact drop time set to " + ChatColor.YELLOW + time + ChatColor.RESET + " seconds.");
		}
	}
	
	private void parseArtifactStart(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!cmd.sender.hasPermission("artifact.control")) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You don't have permission to do this.");
		}
		// If dropper is already running, send error message
		else if(plugin.getArtifactHandler().isRunning()) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "The artifact dropper is already running.");
		}
		// Set dropper to ON
		else {
			plugin.getArtifactHandler().setRunning(true);
		}
	}
	
	private void parseArtifactStop(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!cmd.sender.hasPermission("artifact.control")) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You don't have permission to do this.");
		}
		// If dropper is already running, send error message
		else if(!plugin.getArtifactHandler().isRunning()) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "The artifact dropper is already stopped.");
		}
		// Set dropper to OFF
		else {
			plugin.getArtifactHandler().setRunning(false);
		}
	}
	
	private void parseArtifactState(Cmd cmd) {
		// If sender doesn't have permission, send error message
		if(!cmd.sender.hasPermission("artifact.state")) {
			plugin.getMessenger().sendErrorMessage(cmd.sender, "You don't have permission to do this.");
		}
		else {
			List<String> msgs = new ArrayList<String>();
			SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
			
			// Add drop status message, green for running, red for stopped
			String running = (plugin.getArtifactHandler().isRunning() ? ChatColor.GREEN + "Running" : ChatColor.RED + "Stopped");
			msgs.add("Drop status: " + running);
			
			// Get and format time it takes for drops to refresh
			Date refreshTime = new Date(1000 * plugin.getArtifactHandler().getSeconds());
			msgs.add("Drop refresh time: " + ChatColor.YELLOW + sdf.format(refreshTime));
			
			// Get and format time (mm:ss) until next drop
			Date dropTime = new Date(plugin.getArtifactHandler().getDropTime() - System.currentTimeMillis());
			msgs.add("Time to next drop: " + ChatColor.YELLOW + sdf.format(dropTime));
			
			// Add artifact display name
			String itemName = plugin.getArtifactHandler().getArtifact().getItemMeta().getDisplayName();
			msgs.add("Artifact name: " + itemName);
			
			// Send messages to player
			plugin.getMessenger().sendMessages(cmd.sender, msgs, "Artifact Plugin State");
		}
	}
	
	private void parseArtifactError(Cmd cmd) {
		plugin.getMessenger().sendErrorMessage(cmd.sender, "Command not recognized. Use " 
				+ ChatColor.YELLOW + ChatColor.BOLD + "/artifact help" + ChatColor.RESET + " to see commands.");
	}
	
	private boolean isParsableInteger(String input) {
		try {
			Integer.parseInt(input);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
	/*
	private boolean isParsableBoolean(String input) {
		return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false");
	}
	
	private boolean parseBoolean(String input) {
		return input.equalsIgnoreCase("true");
	}
	
	private boolean isParsableDouble(String input) {
		try {
			Double.parseDouble(input);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	*/
}
