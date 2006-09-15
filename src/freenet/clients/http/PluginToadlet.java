/*
  PluginToadlet.java / Freenet
  Copyright (C) David 'Bombe' Roden'
  Copyright (C) 2005-2006 The Free Network project

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package freenet.clients.http;

import java.io.IOException;
import java.net.URI;

import freenet.client.HighLevelSimpleClient;
import freenet.node.NodeClientCore;
import freenet.plugin.HttpPlugin;
import freenet.plugin.Plugin;
import freenet.plugin.PluginManager;
import freenet.support.HTMLNode;
import freenet.support.MultiValueTable;
import freenet.support.io.Bucket;

/**
 * Toadlet for the plugin manager.
 * 
 * @author David 'Bombe' Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class PluginToadlet extends Toadlet {

	/** The plugin manager backing this toadlet. */
	private final PluginManager pluginManager;
	private final NodeClientCore core;

	/**
	 * Creates a new toadlet.
	 * 
	 * @param client
	 *            The high-level client to use
	 * @param pluginManager
	 *            The plugin manager to use
	 */
	protected PluginToadlet(HighLevelSimpleClient client, PluginManager pluginManager, NodeClientCore core) {
		super(client);
		this.pluginManager = pluginManager;
		this.core = core;
	}

	/**
	 * This toadlet support GET and POST operations.
	 * 
	 * @see freenet.clients.http.Toadlet#supportedMethods()
	 * @return "GET,POST"
	 */
	public String supportedMethods() {
		return "GET,POST";
	}

	/**
	 * Handles a GET request.
	 * 
	 * @see freenet.clients.http.Toadlet#handleGet(java.net.URI,
	 *      freenet.clients.http.ToadletContext)
	 * @param uri
	 *            The URI that was requested
	 * @param ctx
	 *            The context of this toadlet
	 */
	public void handleGet(URI uri, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
		HTTPRequest httpRequest = new HTTPRequest(uri, null, ctx);

		String uriPath = uri.getPath();
		String pluginName = uriPath.substring(uriPath.lastIndexOf('/') + 1);

		if (pluginName.length() > 0) {
			Plugin plugin = findPlugin(pluginName);
			if (plugin != null) {
				if (plugin instanceof HttpPlugin) {
					((HttpPlugin) plugin).handleGet(httpRequest, ctx);
				} else {
					writeReply(ctx, 220, "text/html; charset=utf-8", "OK", createBox(ctx, "Plugin has no web interface", "The plugin does not have a web interface, so there is nothing to show.").toString());
				}
				return;
			}
			writeReply(ctx, 220, "text/html; charset=utf-8", "OK", createBox(ctx, "Plugin not found", "The requested plugin could not be found.").toString());
			return;
		}

		String action = httpRequest.getParam("action");
		if (action.length() == 0) {
			writePermanentRedirect(ctx, "Plugin list", "?action=list");
			return;
		}

		StringBuffer replyBuffer = new StringBuffer();
		if ("list".equals(action)) {
			replyBuffer.append(listPlugins(ctx));
		} else {
			writeReply(ctx, 220, "text/html; charset=utf-8", "OK", createBox(ctx, "Unsupported method", "Unsupported method.").toString());
			return;
		}
		writeReply(ctx, 220, "text/html; charset=utf-8", "OK", replyBuffer.toString());
	}
	
	/**
	 * @see freenet.clients.http.Toadlet#handlePost(java.net.URI, freenet.support.io.Bucket, freenet.clients.http.ToadletContext)
	 */
	public void handlePost(URI uri, Bucket data, ToadletContext ctx) throws ToadletContextClosedException, IOException, RedirectException {
		HTTPRequest httpRequest = new HTTPRequest(uri, data, ctx);
		
		String uriPath = uri.getPath();
		String pluginName = uriPath.substring(uriPath.lastIndexOf('/') + 1);
		
		if (pluginName.length() > 0) {
			Plugin plugin = findPlugin(pluginName);
			if (plugin != null) {
				if (plugin instanceof HttpPlugin) {
					((HttpPlugin) plugin).handlePost(httpRequest, ctx);
				} else {
					writeReply(ctx, 220, "text/html; charset=utf-8", "OK", createBox(ctx, "Plugin has no web interface", "The plugin does not have a web interface, so there is nothing to show.").toString());
				}
				return;
			}
			writeReply(ctx, 220, "text/html; charset=utf-8", "OK", createBox(ctx, "Plugin not found", "The requested plugin could not be found.").toString());
			return;
		}
		
		String pass = httpRequest.getParam("formPassword");
		if((pass == null) || !pass.equals(core.formPassword)) {
			MultiValueTable headers = new MultiValueTable();
			headers.put("Location", "/plugin/");
			ctx.sendReplyHeaders(302, "Found", headers, null, 0);
			return;
		}
		
		String action = httpRequest.getParam("action");
		if (action.length() == 0) {
			writePermanentRedirect(ctx, "Plugin list", "?action=list");
			return;
		}

		StringBuffer replyBuffer = new StringBuffer();
		if ("add".equals(action)) {
			pluginName = httpRequest.getParam("pluginName");
			boolean added = false;
			try {
				pluginManager.addPlugin(pluginName, true);
				added = true;
			} catch (IllegalArgumentException iae1) {
			}
			if (added) {
				writePermanentRedirect(ctx, "Plugin list", "?action=list");
				return;
			}
			replyBuffer.append(createBox(ctx, "Plugin was not loaded", "The plugin you requested could not be loaded. Please verify the name of the plugin\u2019s class and the URL, if you gave one."));
		} else if ("reload".equals(action)) {
			pluginName = httpRequest.getParam("pluginName");
			Plugin plugin = findPlugin(pluginName);
			pluginManager.removePlugin(plugin, false);
			pluginManager.addPlugin(plugin.getClass().getName(), false);
			writePermanentRedirect(ctx, "Plugin list", "?action=list");
			return;
		} else if ("unload".equals(action)) {
			pluginName = httpRequest.getParam("pluginName");
			Plugin plugin = findPlugin(pluginName);
			pluginManager.removePlugin(plugin, true);
			writePermanentRedirect(ctx, "Plugin list", "?action=list");
			return;
		}
		writeReply(ctx, 220, "text/html; charset=utf-8", "OK", replyBuffer.toString());
	}

	/**
	 * Searches the currently installed plugins for the plugin with the
	 * specified internal name.
	 * 
	 * @param internalPluginName
	 *            The internal name of the wanted plugin
	 * @return The wanted plugin, or <code>null</code> if no plugin could be
	 *         found
	 */
	private Plugin findPlugin(String internalPluginName) {
		Plugin[] plugins = pluginManager.getPlugins();
		for (int pluginIndex = 0, pluginCount = plugins.length; pluginIndex < pluginCount; pluginIndex++) {
			Plugin plugin = plugins[pluginIndex];
			String pluginName = plugin.getClass().getName() + "@" + pluginIndex;
			if (pluginName.equals(internalPluginName)) {
				return plugin;
			}
		}
		return null;
	}

	/**
	 * Creates a complete HTML page containing a list of all plugins.
	 * 
	 * @param context
	 *            The toadlet context
	 * @return A StringBuffer containing the HTML page
	 */
	private StringBuffer listPlugins(ToadletContext context) {
		Plugin[] plugins = pluginManager.getPlugins();
		PageMaker pageMaker = context.getPageMaker();
		HTMLNode pageNode = pageMaker.getPageNode("List of Plugins");
		HTMLNode contentNode = pageMaker.getContentNode(pageNode);

		HTMLNode infobox = contentNode.addChild("div", "class", "infobox");
		infobox.addChild("div", "class", "infobox-header", "Plugin list");
		HTMLNode table = infobox.addChild("div", "class", "infobox-content").addChild("table", "class", "plugintable");
		HTMLNode headerRow = table.addChild("tr");
		headerRow.addChild("th", "Plugin Name");
		headerRow.addChild("th", "Internal Name");
		headerRow.addChild("th", "colspan", "3");
		for (int pluginIndex = 0, pluginCount = plugins.length; pluginIndex < pluginCount; pluginIndex++) {
			Plugin plugin = plugins[pluginIndex];
			String internalName = plugin.getClass().getName() + "@" + pluginIndex;
			HTMLNode tableRow = table.addChild("tr");
			tableRow.addChild("td", plugin.getPluginName());
			tableRow.addChild("td", internalName);
			if (plugin instanceof HttpPlugin) {
				tableRow.addChild("td").addChild("form", new String[] { "action", "method" }, new String[] { internalName, "get" }).addChild("input", new String[] { "type", "value" }, new String[] { "submit", "Visit" });
			} else {
				tableRow.addChild("td");
			}
			HTMLNode reloadForm = tableRow.addChild("td").addChild("form", new String[] { "action", "method" }, new String[] { ".", "post" });
			reloadForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "action", "reload" });
			reloadForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "pluginName", internalName });
			reloadForm.addChild("input", new String[] { "type", "value" }, new String[] { "submit", "Reload" });
			reloadForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "formPassword", core.formPassword });
			HTMLNode unloadForm = tableRow.addChild("td").addChild("form", new String[] { "action", "method" }, new String[] { ".", "post" });
			unloadForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "action", "unload" });
			unloadForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "pluginName", internalName });
			unloadForm.addChild("input", new String[] { "type", "value" }, new String[] { "submit", "Unload" });
			unloadForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "formPassword", core.formPassword });
		}

		contentNode.addChild(createAddPluginBox());

		StringBuffer pageBuffer = new StringBuffer();
		pageNode.generate(pageBuffer);
		return pageBuffer;
	}

	/**
	 * Creates an alert box with the specified title and message. A link to the
	 * plugin list is added after the message.
	 * 
	 * @param context
	 *            The toadlet context
	 * @param title
	 *            The title of the box
	 * @param message
	 *            The content of the box
	 * @return A StringBuffer containing the complete page
	 */
	private StringBuffer createBox(ToadletContext context, String title, String message) {
		PageMaker pageMaker = context.getPageMaker();
		HTMLNode pageNode = pageMaker.getPageNode(title);
		HTMLNode contentNode = pageMaker.getContentNode(pageNode);
		HTMLNode infobox = contentNode.addChild("div", "class", "infobox infobox-alert");
		infobox.addChild("div", "class", "infobox-header", title);
		HTMLNode infoboxContent = infobox.addChild("div", "class", "infobox-content");
		infoboxContent.addChild("#", message);
		infoboxContent.addChild("br");
		infoboxContent.addChild("#", "Please ");
		infoboxContent.addChild("a", "href", "?action=list", "return");
		infoboxContent.addChild("#", " to the list of plugins.");
		StringBuffer pageBuffer = new StringBuffer();
		pageNode.generate(pageBuffer);
		return pageBuffer;
	}

	/**
	 * Appends the HTML code for the &ldquo;add plugin&rdquo; box to the given
	 * StringBuffer.
	 * 
	 * @param outputBuffer
	 *            The StringBuffer to append the HTML code to
	 */
	private HTMLNode createAddPluginBox() {
		HTMLNode addPluginBox = new HTMLNode("div", "class", "infobox");
		addPluginBox.addChild("div", "class", "infobox-header", "Add a plugin");
		HTMLNode addForm = addPluginBox.addChild("div", "class", "infobox-content").addChild("form", new String[] { "action", "method" }, new String[] { ".", "post" });
		addForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "action", "add" });
		addForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "hidden", "formPassword", core.formPassword });
		addForm.addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "text", "pluginName", "", "40" });
		addForm.addChild("input", new String[] { "type", "value" }, new String[] { "submit", "Load plugin" });
		return addPluginBox;
	}

}
