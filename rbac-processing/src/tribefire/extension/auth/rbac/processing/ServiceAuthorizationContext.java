package tribefire.extension.auth.rbac.processing;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

public class ServiceAuthorizationContext {
	private Function<String, CmdResolver> mdResolverLookup;
	private Set<String> overrideRoles = Collections.emptySet();
	
	@Required
	public void setMdResolverLookup(Function<String, CmdResolver> mdResolverLookup) {
		this.mdResolverLookup = mdResolverLookup;
	}
	
	@Configurable
	public void setOverrideRoles(Set<String> overrideRoles) {
		this.overrideRoles = overrideRoles;
	}

	/** @deprecated Use {@link #setOverrideRoles(Set)}. */
	@Deprecated
	@Configurable
	public void setBypassRoles(Set<String> bypassRoles) {
		setOverrideRoles(bypassRoles);
	}
	
	public Function<String, CmdResolver> getMdResolverLookup() {
		return mdResolverLookup;
	}
	
	public Set<String> getOverrideRoles() {
		return overrideRoles;
	}

	/** @deprecated Use {@link #getOverrideRoles()}. */
	@Deprecated
	public Set<String> getBypassRoles() {
		return getOverrideRoles();
	}
	
	public ServiceDomainAuthorizationResolver getDomainResolver(String domainId) {
		return new ServiceDomainAuthorizationResolver(mdResolverLookup.apply(domainId), overrideRoles);
	}
}
