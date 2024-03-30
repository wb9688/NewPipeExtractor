package org.schabi.newpipe.extractor.subscription

import java.io.Serializable

class SubscriptionItem(@JvmField val serviceId: Int, @JvmField val url: String?, @JvmField val name: String?) : Serializable {

    public override fun toString(): String {
        return (javaClass.getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "[name=" + name + " > " + serviceId + ":" + url + "]")
    }
}
