package com.andrei1058.bedwars.proxy.arenamanager;

import com.andrei1058.bedwars.proxy.BedWarsProxy;
import com.andrei1058.bedwars.proxy.api.*;
import com.andrei1058.bedwars.proxy.api.event.ArenaCacheRemoveEvent;
import com.andrei1058.bedwars.proxy.configuration.ConfigPath;
import com.andrei1058.bedwars.proxy.language.LanguageManager;
import com.andrei1058.bedwars.proxy.socketmanager.ArenaSocketTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.andrei1058.bedwars.proxy.BedWarsProxy.config;
import static com.andrei1058.bedwars.proxy.BedWarsProxy.getParty;

public class ArenaManager implements BedWars.ArenaUtil {

    private final LinkedList<CachedArena> arenas = new LinkedList<>();
    private final HashMap<String, ArenaSocketTask> socketByServer = new HashMap<>();

    private static ArrayList<ArrayList<CachedArena>> queue = new ArrayList<>();

    private static ArrayList<String> groups = new ArrayList<>();

    private static ArrayList<CachedArena> randomQueue = new ArrayList<>();

    private static ArenaManager instance = null;

    private ArenaManager() {
        instance = this;
    }

    public static ArenaManager getInstance() {
        return instance == null ? new ArenaManager() : instance;
    }

    public void registerServerSocket(String server, ArenaSocketTask task) {
        if (socketByServer.containsKey(server)) {
            socketByServer.replace(server, task);
            return;
        }
        socketByServer.put(server, task);
    }

    public void randomQueuemaker(){
        Bukkit.getScheduler().runTaskAsynchronously(BedWarsProxy.getPlugin(), () -> {
            randomQueue.addAll(getArenas());
            if (config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_RANDOMARENAS)) {
                Collections.shuffle(randomQueue);
            }
        });
    }

    public void createQueue(){
        Bukkit.getServer().getLogger().info("Running Create Queue method");
        if (getArenas().isEmpty())
            return;
        Bukkit.getScheduler().runTaskAsynchronously(BedWarsProxy.getPlugin(), () -> {
            Bukkit.getServer().getLogger().info(getArenas().toString());
            if (groups.isEmpty())
                for (CachedArena a : getArenas()) {
                    if (!groups.contains(a.getArenaGroup().toLowerCase())) {
                        Bukkit.getServer().getLogger().info("Adding thing " + a.getArenaName());
                        groups.add(a.getArenaGroup());
                        Bukkit.getServer().getLogger().info(groups.toString());
                    }
                }
            Bukkit.getServer().getLogger().info(groups.toString());

            ArrayList<CachedArena> holder = new ArrayList<>(getArenas());

                for (int i = 0; i < groups.size(); i++) {
                    String groupname = groups.get(i);
                    Bukkit.getLogger().info(groupname + "We are in the loop");
                    if (queue.get(i).isEmpty()){
                        ArrayList<CachedArena> Arena9 = new ArrayList<>();
                        for (int j = 0; j < getArenas().size(); j++){
                            CachedArena a1 = getArenas().get(j);
                            if (a1.getArenaGroup().equalsIgnoreCase(groupname)){
                                Arena9.add(a1);
                                Bukkit.getLogger().info(Arena9.toString());
                            }
                        }
                        if (config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_RANDOMARENAS)) {
                            Collections.shuffle(Arena9);
                        }
                        Bukkit.getServer().getLogger().info(Arena9.toString());
                        queue.add(Arena9);
                    }
                }
        });
        Bukkit.getServer().getLogger().info(queue.get(0).get(0).getArenaName());
    }

    public void registerArena(@NotNull CachedArena arena) {
        if (getArena(arena.getServer(), arena.getRemoteIdentifier()) != null) return;
        arenas.add(arena);
    }

    public CachedArena getArena(String server, String remoteIdentifier) {

        List<CachedArena> arenaList = getArenas();

        for (CachedArena ca : arenaList) {
            if (ca.getServer().equals(server) && ca.getRemoteIdentifier().equals(remoteIdentifier)) return ca;
        }

        return null;
    }

    public static List<CachedArena> getArenas() {
        return Collections.unmodifiableList(getInstance().arenas);
    }

    public static Comparator<? super CachedArena> getComparator() {
        return new Comparator<CachedArena>() {
            @Override
            public int compare(CachedArena o1, CachedArena o2) {
                if (o1.getStatus() == ArenaStatus.STARTING && o2.getStatus() == ArenaStatus.STARTING) {
                    return Integer.compare(o2.getCurrentPlayers(), o1.getCurrentPlayers());
                }
                else if (o1.getStatus() == ArenaStatus.STARTING && o2.getStatus() != ArenaStatus.STARTING) {
                    return -1;
                }
                else if (o2.getStatus() == ArenaStatus.STARTING && o1.getStatus() != ArenaStatus.STARTING) {
                    return 1;
                }
                else if (o1.getStatus() == ArenaStatus.WAITING && o2.getStatus() == ArenaStatus.WAITING) {
                    // balance nodes
                    if (o1.getServer().equals(o2.getServer())){
                        return -1;
                    }
                    return Integer.compare(o2.getCurrentPlayers(), o1.getCurrentPlayers());
                }
                else if (o1.getStatus() == ArenaStatus.WAITING && o2.getStatus() != ArenaStatus.WAITING) {
                    return -1;
                }
                else if (o2.getStatus() == ArenaStatus.WAITING && o1.getStatus() != ArenaStatus.WAITING) {
                    return 1;
                }
                else if (o1.getStatus() == ArenaStatus.PLAYING && o2.getStatus() == ArenaStatus.PLAYING) {
                    return -1;
                }
                else if (o1.getStatus() == ArenaStatus.PLAYING && o2.getStatus() != ArenaStatus.PLAYING) {
                    return -1;
                }
                else
                    return 1;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof CachedArena;
            }
        };
    }

    public static ArenaSocketTask getSocketByServer(String server) {
        return getInstance().socketByServer.getOrDefault(server, null);
    }

    /**
     * Check if given string is an integer.
     */
    @SuppressWarnings("unused")
    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public ArrayList<CachedArena> reorder(ArrayList<CachedArena> c){
        Bukkit.getServer().getLogger().info(c.get(0).getArenaName());
        Bukkit.getServer().getLogger().info("Reorder 1");
        for (int i = 0; i < c.size(); i++){
            for (int j = 0; j < c.size(); j++){
                if (i == j)
                    continue;
                else if ((i > j) && (c.get(i).getCurrentPlayers() > c.get(j).getCurrentPlayers()))
                {
                    CachedArena hold = c.get(i);
                    c.set(i, c.get(j));
                    c.set(j, hold);
                    continue;
                }
                else if ((i < j) && (c.get(i).getCurrentPlayers() < c.get(j).getCurrentPlayers()))
                {
                    CachedArena hold = c.get(i);
                    c.set(i, c.get(j));
                    c.set(j, hold);
                    continue;
                }
            }
        }
        Bukkit.getServer().getLogger().info(c.get(0).getArenaName());
        Bukkit.getServer().getLogger().info("Reorder 2");
        return c;
    }

    public void updateQueue(){
        Bukkit.getScheduler().runTaskAsynchronously(BedWarsProxy.getPlugin(), () -> {
            for (ArrayList<CachedArena> test : queue) {
                for (CachedArena a : test) {
                    if ((a.getStatus() == ArenaStatus.PLAYING)  || (a.getStatus() == ArenaStatus.RESTARTING)){
                        queue.get(queue.indexOf(test)).remove((queue.get(queue.indexOf(test)).indexOf(a)));
                    }
                }
            }
        });
    }

    /**
     * Add a player to the most filled arena from a group.
     */
    public boolean joinRandomFromGroup(@NotNull Player p, String group) {
        //rewrite by JT122406
        //checks for party leader
        if (getParty().hasParty(p.getUniqueId()) && !getParty().isOwner(p.getUniqueId())) {
            p.sendMessage(LanguageManager.get().getMsg(p, Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER));
            return false;
        }else if (getArenas().isEmpty())
        {
            p.sendMessage(LanguageManager.get().getMsg(p, Messages.COMMAND_JOIN_NO_EMPTY_FOUND));
            return true;
        }
        //Bukkit.getServer().getLogger().info(queue.get(0).get(0).getArenaName());
        Bukkit.getScheduler().runTaskAsynchronously(BedWarsProxy.getPlugin(), () -> {

            int amount = BedWarsProxy.getParty().hasParty(p.getUniqueId()) ? BedWarsProxy.getParty().getMembers(p.getUniqueId()).size() : 1;
            if (queue.isEmpty()){
                createQueue();
                Bukkit.getServer().getLogger().info("Hit this!");
            }else {
                for (ArrayList test: queue) {
                    if (test.isEmpty()){
                        createQueue();
                    }
                }
            }
            int i = 0;
            ArrayList<CachedArena> ArenaList = new ArrayList<>();
            for (ArrayList<CachedArena> pog: queue) {
                if (pog.get(0) == null){
                    continue;
                }
                else
                {
                    if (pog.get(0).getArenaName().equalsIgnoreCase(group)){
                        ArenaList.addAll(pog);
                        i = queue.indexOf(pog);
                        break;
                    }
                }
            }

            //puts only arenas from group into arraylist
            List<CachedArena> arenaList = reorder(ArenaList);
            if (arenaList.isEmpty()) {
                p.sendMessage(LanguageManager.get().getMsg(p, Messages.COMMAND_JOIN_NO_EMPTY_FOUND));
                return;
            }
            //You are here JT
            for (CachedArena current : arenaList) {
                if ((current.getArenaGroup().equalsIgnoreCase(group)) && ((current.getMaxPlayers() - current.getCurrentPlayers()) > amount) && (((current.getStatus() == ArenaStatus.WAITING) || (current.getStatus() == ArenaStatus.STARTING))))
                    Bukkit.getScheduler().runTask(BedWarsProxy.getPlugin(), () -> {
                        current.addPlayer(p, null);
                    });
                else if ((current.getArenaGroup().equalsIgnoreCase(group))  && ((current.getMaxPlayers() - current.getCurrentPlayers()) == amount) && (((current.getStatus() == ArenaStatus.WAITING) || (current.getStatus() == ArenaStatus.STARTING)))) {
                    Bukkit.getScheduler().runTask(BedWarsProxy.getPlugin(), () -> {
                        current.addPlayer(p, null);  //Perfect fit conditions
                    });
                    if (queue.get(i).contains(current)){
                        queue.get(i).remove(current);
                    }
                    return;
                }
            }
        });
        updateQueue();
        return true;
    }

    /**
     * Check if arena group exists.
     */
    public static boolean hasGroup(String arenaGroup) {
        for (CachedArena ad : getArenas()) {
            if (ad.getArenaGroup().equalsIgnoreCase(arenaGroup)) return true;
        }
        return false;
    }

    /**
     * Add a player to the most filled arena.
     * Check if is the party owner first.
     */
    public boolean joinRandomArena(@NotNull Player p) {
        //rewrite by JT122406
        //checks for party leader
        if (getParty().hasParty(p.getUniqueId()) && !getParty().isOwner(p.getUniqueId())) {
            p.sendMessage(LanguageManager.get().getMsg(p, Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER));
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(BedWarsProxy.getPlugin(), () -> {
            //puts only arenas from group into arraylist
            List<CachedArena> arenaList = new ArrayList<>();
            int amount = BedWarsProxy.getParty().hasParty(p.getUniqueId()) ? BedWarsProxy.getParty().getMembers(p.getUniqueId()).size() : 1;
            for (CachedArena current : getArenas()) {
                if (((current.getMaxPlayers() - current.getCurrentPlayers()) >= amount) && (((current.getStatus() == ArenaStatus.WAITING) || (current.getStatus() == ArenaStatus.STARTING))))
                    arenaList.add(current);
                else if (((current.getMaxPlayers() - current.getCurrentPlayers()) == amount) && (((current.getStatus() == ArenaStatus.WAITING) || (current.getStatus() == ArenaStatus.STARTING)))) {
                    Bukkit.getScheduler().runTask(BedWarsProxy.getPlugin(), () -> {
                        current.addPlayer(p, null); //Perfect fit conditions
                    });
                    return;
                }
            }

            if (arenaList.isEmpty()){
                p.sendMessage(LanguageManager.get().getMsg(p, Messages.COMMAND_JOIN_NO_EMPTY_FOUND));
                return;
            }

            //shuffle if determined in config
            if (config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_RANDOMARENAS)){
                Collections.shuffle(arenaList);
                //randomize it then we will sort by players in arena
            }


            Bukkit.getScheduler().runTask(BedWarsProxy.getPlugin(), () -> {
                CachedArena hold = arenaList.get(0);
                //Reorder based on players in game
                for (int i = 1; i < arenaList.size(); i++) {
                    if (arenaList.get(i).getCurrentPlayers() > hold.getCurrentPlayers() && (arenaList.get(i).getMaxPlayers() - arenaList.get(i).getCurrentPlayers() >= amount))
                        hold = arenaList.get(i);
                    else if ((arenaList.get(i).getMaxPlayers() - arenaList.get(i).getCurrentPlayers()) == amount){  //If there is exactly the amount of players in the party left in a waiting arena join that arena and break to save process time
                        arenaList.get(i).addPlayer(p, null);
                        return;
                    }
                }
                hold.addPlayer(p, null);
            });

        });

        return true;
    }

    public void disableArena(CachedArena a) {
        arenas.remove(a);
        Bukkit.getPluginManager().callEvent(new ArenaCacheRemoveEvent(a));
    }

    public HashMap<String, ArenaSocketTask> getSocketByServer() {
        return socketByServer;
    }

    @SuppressWarnings("unused")
    @Nullable
    public static CachedArena getArenaByIdentifier(String identifier) {
        for (CachedArena ca : getArenas()) {
            if (ca.getRemoteIdentifier().equals(identifier)) {
                return ca;
            }
        }
        return null;
    }

    @Override
    public void destroyReJoins(CachedArena arena) {
        List<RemoteReJoin> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, RemoteReJoin> rrj : com.andrei1058.bedwars.proxy.rejoin.RemoteReJoin.getRejoinByUUID().entrySet()) {
            if (rrj.getValue().getArena().equals(arena)) {
                toRemove.add(rrj.getValue());
            }
        }
        toRemove.forEach(RemoteReJoin::destroy);
    }

    @Override
    public RemoteReJoin getReJoin(UUID player) {
        return com.andrei1058.bedwars.proxy.rejoin.RemoteReJoin.getReJoin(player);
    }
}