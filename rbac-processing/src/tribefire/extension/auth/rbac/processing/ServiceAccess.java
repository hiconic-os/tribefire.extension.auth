package tribefire.extension.auth.rbac.processing;

public enum ServiceAccess {
	ALLOWED_PUBLIC(true),
	ALLOWED_SUPERUSER(true),
	ALLOWED(true),
	DENIED(false)
	;
	
	private boolean allowed;
	
	private ServiceAccess(boolean allowed) {
		this.allowed = allowed;
	}
	
	boolean allowed() {
		return allowed;
	}
}
