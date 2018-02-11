package com.axzae.homeassistant.shared;

import com.axzae.homeassistant.model.rest.RxPayload;

import io.reactivex.subjects.Subject;

public interface EventEmitterInterface {
    Subject<RxPayload> getEventSubject();
}
