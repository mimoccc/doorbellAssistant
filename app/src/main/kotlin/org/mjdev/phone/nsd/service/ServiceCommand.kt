package org.mjdev.phone.nsd.service

abstract class ServiceCommand {
    companion object {
        object GetNsdDevice : ServiceCommand()
        object GetState : ServiceCommand()
    }
}
