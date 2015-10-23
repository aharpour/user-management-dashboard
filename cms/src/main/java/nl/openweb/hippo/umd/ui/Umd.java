package nl.openweb.hippo.umd.ui;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.perspective.Perspective;
import org.hippoecm.frontend.service.IconSize;

public class Umd extends Perspective {

    private static final long serialVersionUID = 1L;
    private static final CssResourceReference PERSPECTIVE_SKIN = new CssResourceReference(Umd.class,
            "user-management-dashboard.css");

    public Umd(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
    }

    @Override
    public IModel<String> getTitle() {
        return new StringResourceModel("label.title", this, null, "default value", "arg1");
    }

    @Override
    public ResourceReference getIcon(IconSize type) {
        return new PackageResourceReference(Umd.class, "user-management-dashboard-" + type.getSize() + ".png");
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(PERSPECTIVE_SKIN));
    }

}
