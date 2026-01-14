package org.mjdev.doorbellassistant.vpn;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.mjdev.doorbellassistant.BuildConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration {
    public static final Gson GSON = new Gson();
    static final int VERSION = 2;
    static final int MINOR_VERSION = 3;
    private static final String TAG = "Configuration";
    public int version = 1;
    public int minorVersion = 0;
    public boolean autoStart;
    public Hosts hosts = new Hosts();
    public DnsServers dnsServers = new DnsServers();
    @SerializedName(value = "allowlist", alternate = "whitelist")
    public Allowlist allowlist = new Allowlist();
    public boolean showNotification = true;
    public boolean watchDog = false;
    public boolean ipV6Support = true;

    public static Configuration read(Reader reader) throws IOException {
        Configuration config = GSON.fromJson(reader, Configuration.class);
        if (config.version > VERSION)
            throw new IOException("Unhandled file format version");
        for (int i = config.minorVersion + 1; i <= MINOR_VERSION; i++) {
            config.runUpdate(i);
        }
        //noinspection HttpUrlsUsage
        config.updateURL(
                "http://someonewhocares.org/hosts/hosts",
                "https://someonewhocares.org/hosts/hosts",
                0
        );
        return config;
    }

    @SuppressWarnings("HttpUrlsUsage")
    public void runUpdate(int level) {
        switch (level) {
            case 1:
                /* Switch someonewhocares to https */
                updateURL("http://someonewhocares.org/hosts/hosts", "https://someonewhocares.org/hosts/hosts", -1);
                /* Switch to StevenBlack's host file */
                addURL(0, "StevenBlack's hosts file (includes all others)",
                        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
                        Item.STATE_DENY);
                updateURL("https://someonewhocares.org/hosts/hosts", null, Item.STATE_IGNORE);
                updateURL("https://adaway.org/hosts.txt", null, Item.STATE_IGNORE);
                updateURL("https://www.malwaredomainlist.com/hostslist/hosts.txt", null, Item.STATE_IGNORE);
                updateURL("https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=1&mimetype=plaintext", null, Item.STATE_IGNORE);
                /* Remove broken host */
                removeURL("http://winhelp2002.mvps.org/hosts.txt");
                /* Update digitalcourage dns and add cloudflare */
                updateDNS("85.214.20.141", "46.182.19.48");
                addDNS("CloudFlare DNS (1)", "1.1.1.1", false);
                addDNS("CloudFlare DNS (2)", "1.0.0.1", false);
                break;
            case 2:
                removeURL("https://hosts-file.net/ad_servers.txt");
                break;
            case 3:
                disableURL("https://blokada.org/blocklists/ddgtrackerradar/standard/hosts.txt");
        }
        this.minorVersion = level;
    }

    public void updateURL(String oldURL, String newURL, int newState) {
        for (Item host : hosts.items) {
            if (host.location.equals(oldURL)) {
                if (newURL != null)
                    host.location = newURL;
                if (newState >= 0)
                    host.state = newState;
            }
        }
    }

    public void updateDNS(String oldIP, String newIP) {
        for (Item host : dnsServers.items) {
            if (host.location.equals(oldIP))
                host.location = newIP;
        }
    }

    public void addDNS(String title, String location, boolean isEnabled) {
        Item item = new Item();
        item.title = title;
        item.location = location;
        item.state = isEnabled ? 1 : 0;
        dnsServers.items.add(item);
    }

    public void addURL(int index, String title, String location, int state) {
        Item item = new Item();
        item.title = title;
        item.location = location;
        item.state = state;
        hosts.items.add(index, item);
    }

    public void removeURL(String oldURL) {
        hosts.items.removeIf(host -> host.location.equals(oldURL));
    }

    public void disableURL(String oldURL) {
        Log.d(TAG, String.format("disableURL: Disabling %s", oldURL));
        for (Item host : hosts.items) {
            if (host.location.equals(oldURL))
                host.state = Item.STATE_IGNORE;
        }
    }

    public void write(Writer writer) throws IOException {
        GSON.toJson(this, writer);
    }

    public static class Item {
        public static final int STATE_IGNORE = 2;
        public static final int STATE_DENY = 0;
        public static final int STATE_ALLOW = 1;
        public String title;
        public String location;
        public int state;

        public boolean isDownloadable() {
            //noinspection HttpUrlsUsage
            return location.startsWith("https://") || location.startsWith("http://");
        }
    }

    public static class Hosts {
        public boolean enabled;
        public boolean automaticRefresh = false;
        public List<Item> items = new ArrayList<>();
    }

    public static class DnsServers {
        public boolean enabled;
        public List<Item> items = new ArrayList<>();
    }

    @SuppressLint("QueryPermissionsNeeded")
    public static class Allowlist {
        public static final int DEFAULT_MODE_ON_VPN = 0;
        public static final int DEFAULT_MODE_NOT_ON_VPN = 1;
        public static final int DEFAULT_MODE_INTELLIGENT = 2;

        public int defaultMode = DEFAULT_MODE_ON_VPN;
        public List<String> itemsNotOnVpn = new ArrayList<>();
        public List<String> itemsOnVpn = new ArrayList<>();

        public void resolve(
                PackageManager pm,
                Set<String> onVpn,
                Set<String> notOnVpn
        ) {
            Set<String> webBrowserPackageNames = new HashSet<>();
             List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(newBrowserIntent(), 0);
            for (ResolveInfo resolveInfo : resolveInfoList) {
                webBrowserPackageNames.add(resolveInfo.activityInfo.packageName);
            }
            webBrowserPackageNames.add("com.google.android.webview");
            webBrowserPackageNames.add("com.android.htmlviewer");
            webBrowserPackageNames.add("com.google.android.backuptransport");
            webBrowserPackageNames.add("com.google.android.gms");
            webBrowserPackageNames.add("com.google.android.gsf");
            for (ApplicationInfo applicationInfo : pm.getInstalledApplications(0)) {
                if (applicationInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                    onVpn.add(applicationInfo.packageName);
                } else if (itemsOnVpn.contains(applicationInfo.packageName)) {
                    onVpn.add(applicationInfo.packageName);
                } else if (itemsNotOnVpn.contains(applicationInfo.packageName)) {
                    notOnVpn.add(applicationInfo.packageName);
                } else if (defaultMode == DEFAULT_MODE_ON_VPN) {
                    onVpn.add(applicationInfo.packageName);
                } else if (defaultMode == DEFAULT_MODE_NOT_ON_VPN) {
                    notOnVpn.add(applicationInfo.packageName);
                } else if (defaultMode == DEFAULT_MODE_INTELLIGENT) {
                    if (webBrowserPackageNames.contains(applicationInfo.packageName))
                        onVpn.add(applicationInfo.packageName);
                    else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                        notOnVpn.add(applicationInfo.packageName);
                    else
                        onVpn.add(applicationInfo.packageName);
                }
            }
        }

        Intent newBrowserIntent() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://isabrowser.dns66.jak-linux.org/"));
            return intent;
        }
    }
}
