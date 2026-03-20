package tribefire.extension.auth.rbac.processing;

import java.util.Set;

import com.braintribe.model.meta.GmEntityType;

public record ServiceAuthorization(GmEntityType type, Set<String> overrideRoles, Set<String> allowRoles, Set<String> denyRoles) {
	public boolean isPriviledged() {
		return !allowRoles.isEmpty() || !denyRoles.isEmpty();
	}
	
	public boolean isPublic() {
		return !isPriviledged();
	}
	
	public ServiceAccess determineAccess(Set<String> effectiveRoles) {
		if (isPublic())
			return ServiceAccess.ALLOWED_PUBLIC;
		
		if (isOverridden(effectiveRoles))
			return ServiceAccess.ALLOWED_SUPERUSER;
		
		if (!isAnyRoleDenied(effectiveRoles) && isAnyRoleAllowed(effectiveRoles))
			return ServiceAccess.ALLOWED;
		
		return ServiceAccess.DENIED;
	}
	
	public boolean isAllowed(Set<String> effectiveRoles) {
		return determineAccess(effectiveRoles).allowed();
	}
	
	public boolean isOverridden(Set<String> effectiveRoles) {
		return hasAnyRole(effectiveRoles, overrideRoles);
	}
	
	public boolean isAnyRoleAllowed(Set<String> effectiveRoles) {
		if (allowRoles.isEmpty())
			return true;
		
		if (hasAnyRole(effectiveRoles, allowRoles)) 
			return true;
		
		return false;
	}
	
	public boolean isAnyRoleDenied(Set<String> effectiveRoles) {
		if (denyRoles.isEmpty())
			return false;
		
		return hasAnyRole(effectiveRoles, denyRoles); 
	}
	
	private boolean hasAnyRole(Set<String> effectiveRoles, Set<String> roles) {
		for (String role : roles) {
			if (effectiveRoles.contains(role)) {
				return true;
			}
		}
		
		return false;
	}
}
