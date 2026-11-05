package tribefire.extension.auth.rbac.rx.api;

import hiconic.rx.module.api.service.ModelSymbol;
import tribefire.extension.auth._RbacApiModel_;

/** Identities for platform-owned Rx configuration models; no mutable metadata is stored in reflection models. */
public interface RbacModelSymbols {
    ModelSymbol configuredRbacApiModel = ModelSymbol.configured(_RbacApiModel_.reflection);
}
