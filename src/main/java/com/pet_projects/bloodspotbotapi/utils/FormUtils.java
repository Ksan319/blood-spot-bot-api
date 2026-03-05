package com.pet_projects.bloodspotbotapi.utils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;

public class FormUtils {

    @SuppressWarnings("null")
    public static MultiValueMap<String, String> toFormData(Object object) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (value != null) {
                    form.add(field.getName(), value.toString());
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return form;
    }
}
