package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.datetime.DateTimeFieldValueSource;
import com.scottlogic.deg.generator.restrictions.DateTimeRestrictions;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TemporalFieldValueSourceFactory implements DataTypeFactory {
    public static TypeDefinition getTypeDefinition() {
        return new TypeDefinition(new TemporalFieldValueSourceFactory());
    }

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        DateTimeRestrictions restrictions = fieldSpec.getDateTimeRestrictions();

        return new DateTimeFieldValueSource(
            restrictions != null ? restrictions : new DateTimeRestrictions(),
            getBlacklist(fieldSpec));
    }

    private static Set<Object> getBlacklist(FieldSpec fieldSpec) {
        if (fieldSpec.getSetRestrictions() == null)
            return Collections.emptySet();

        return new HashSet<>(fieldSpec.getSetRestrictions().getBlacklist());
    }

    @Override
    public DataGeneratorBaseTypes getUnderlyingDataType() {
        return DataGeneratorBaseTypes.TEMPORAL;
    }

    @Override
    public boolean isValid(Object value, FieldSpec fieldSpec) {
        DateTimeRestrictions temporalRestrictions = fieldSpec.getDateTimeRestrictions();
        return value instanceof OffsetDateTime && (temporalRestrictions == null || temporalRestrictions.match(value));
    }

    @Override
    public boolean canProduceAnyValues(FieldSpec fieldSpec) {
        return true; //Note: Contradiction checking happens in the restriction merge operations for primitive types
    }
}
