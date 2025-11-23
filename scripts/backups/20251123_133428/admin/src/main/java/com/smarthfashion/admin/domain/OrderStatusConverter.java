package com.smarthfashion.admin.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.text.Normalizer;
import java.util.Locale;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String normalized = normalize(dbData);
        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // If the database holds an unknown/legacy value, return null to avoid breaking reads
            return null;
        }
    }

    private static String normalize(String in) {
        String s = in.trim();
        // Remove accents
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // Upper-case and replace spaces/hyphens with underscore
        s = s.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return s;
    }
}
