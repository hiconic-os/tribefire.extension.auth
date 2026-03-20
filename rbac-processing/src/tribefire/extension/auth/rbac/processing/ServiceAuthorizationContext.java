package tribefire.extension.auth.rbac.processing;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

public class ServiceAuthorizationContext {
	private Function<String, CmdResolver> mdResolverLookup;
	private Set<String> bypassRoles = Collections.emptySet();
	
	@Required
	public void setMdResolverLookup(Function<String, CmdResolver> mdResolverLookup) {
		this.mdResolverLookup = mdResolverLookup;
	}
	
	@Configurable
	public void setBypassRoles(Set<String> bypassRoles) {
		this.bypassRoles = bypassRoles;
	}
	
	public Function<String, CmdResolver> getMdResolverLookup() {
		return mdResolverLookup;
	}
	
	public Set<String> getBypassRoles() {
		return bypassRoles;
	}
	
	public ServiceDomainAuthorizationResolver getDomainResolver(String domainId) {
		return new ServiceDomainAuthorizationResolver(mdResolverLookup.apply(domainId), bypassRoles);
	}
}
