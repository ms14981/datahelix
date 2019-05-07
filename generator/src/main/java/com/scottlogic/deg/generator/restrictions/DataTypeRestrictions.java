package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.TypeDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class DataTypeRestrictions implements TypeRestrictions {

    public final static TypeRestrictions ALL_TYPES_PERMITTED = new AnyTypeRestriction();
    public final static TypeRestrictions NO_TYPES_PERMITTED = new NoAllowedTypesRestriction();

    private final Set<TypeDefinition> allowedTypes;

    public DataTypeRestrictions(Collection<TypeDefinition> allowedTypes) {
        if (allowedTypes.size() == 0)
            throw new UnsupportedOperationException("Cannot have a type restriction with no types");

        this.allowedTypes = new HashSet<>(allowedTypes);
    }

    public static TypeRestrictions createFromWhiteList(TypeDefinition... types) {
        return new DataTypeRestrictions(Arrays.asList(types));
    }

    public TypeRestrictions except(TypeDefinition... types) {
        if (types.length == 0)
            return this;

        List<TypeDefinition> allowedTypes = this.allowedTypes
            .stream()
            .filter(allowedType -> Arrays.stream(types).noneMatch(t -> allowedType.getBaseType().equals(t.getBaseType())))
            .collect(Collectors.toList());

        if (allowedTypes.isEmpty()){
            return NO_TYPES_PERMITTED;
        }

        return new DataTypeRestrictions(allowedTypes);
    }

    public boolean isTypeAllowed(TypeDefinition type){
        return allowedTypes.contains(type);
    }

    public String toString() {
        if (allowedTypes.size() == 1)
            return String.format("Type = %s", allowedTypes.toArray()[0]);

        return String.format(
                "Types: %s",
                Objects.toString(allowedTypes));
    }

    public TypeRestrictions intersect(TypeRestrictions other) {
        if (other == ALL_TYPES_PERMITTED)
            return this;

        ArrayList<TypeDefinition> allowedTypes = new ArrayList<>(this.allowedTypes);
        allowedTypes.retainAll(other.getAllowedTypes());

        if (allowedTypes.isEmpty())
            return null;

        //micro-optimisation; if there is only one value in allowedTypes then there must have been only one value in either this.allowedTypes or other.allowedTypes
        if (allowedTypes.size() == 1) {
            return other.getAllowedTypes().size() == 1
                    ? other
                    : this;
        }

        return new DataTypeRestrictions(allowedTypes);
    }

    public Set<TypeDefinition> getAllowedTypes() {
        return allowedTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataTypeRestrictions that = (DataTypeRestrictions) o;
        return Objects.equals(allowedTypes, that.allowedTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedTypes);
    }
}

