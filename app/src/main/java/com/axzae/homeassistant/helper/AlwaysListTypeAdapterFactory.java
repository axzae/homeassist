/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.axzae.homeassistant.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AlwaysListTypeAdapterFactory<E> implements TypeAdapterFactory {

    // Gson can instantiate it itself
    private AlwaysListTypeAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
        // If it's not a List -- just delegate the job to Gson and let it pick the best type adapter itself
        if (!List.class.isAssignableFrom(typeToken.getRawType())) {
            return null;
        }
        // Resolving the list parameter type
        final Type elementType = resolveTypeArgument(typeToken.getType());
        @SuppressWarnings("unchecked") final TypeAdapter<E> elementTypeAdapter = (TypeAdapter<E>) gson.getAdapter(TypeToken.get(elementType));
        // Note that the always-list type adapter is made null-safe, so we don't have to check nulls ourselves
        @SuppressWarnings("unchecked") final TypeAdapter<T> alwaysListTypeAdapter = (TypeAdapter<T>) new AlwaysListTypeAdapter<>(elementTypeAdapter).nullSafe();
        return alwaysListTypeAdapter;
    }

    private static Type resolveTypeArgument(final Type type) {
        // The given type is not parameterized?
        if (!(type instanceof ParameterizedType)) {
            // No, raw
            return Object.class;
        }
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        return parameterizedType.getActualTypeArguments()[0];
    }

    private static final class AlwaysListTypeAdapter<E> extends TypeAdapter<List<E>> {

        private final TypeAdapter<E> elementTypeAdapter;

        private AlwaysListTypeAdapter(final TypeAdapter<E> elementTypeAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
        }

        @Override
        public void write(final JsonWriter out, final List<E> list) {
            new GsonBuilder().create().toJson(list, List.class, out);
            //throw new UnsupportedOperationException();
        }

        @Override
        public List<E> read(final JsonReader in) throws IOException {
            // This is where we detect the list "type"
            final List<E> list = new ArrayList<>();
            final JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    // If it's a regular list, just consume [, <all elements>, and ]
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(elementTypeAdapter.read(in));
                    }
                    in.endArray();
                    break;
                case BEGIN_OBJECT:
                case STRING:
                case NUMBER:
                case BOOLEAN:
                    // An object or a primitive? Just add the current value to the result list
                    list.add(elementTypeAdapter.read(in));
                    break;
                case NULL:
                    throw new AssertionError("Must never happen: check if the type adapter configured with .nullSafe()");
                case NAME:
                case END_ARRAY:
                case END_OBJECT:
                case END_DOCUMENT:
                    throw new MalformedJsonException("Unexpected token: " + token);
                default:
                    throw new AssertionError("Must never happen: " + token);
            }
            return list;
        }

    }

}