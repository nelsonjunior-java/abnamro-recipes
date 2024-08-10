package com.abnamro.recipes_api.infra.messaging;

import java.io.Serializable;

/**
 * Marker interface for messages that are intended to be sent through the messaging infrastructure.
 * <p>
 * All message objects must implement this interface, ensuring they are serializable.
 * </p>
 */
public interface Message extends Serializable {

}
