package umlGenerator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author shahs
 */
public class Activator extends AbstractUIPlugin {

    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "UmlGenerator";

    /**
     * The shared instance.
     */
    private static Activator plugin;

    /**
     * The constructor.
     */
    public Activator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
