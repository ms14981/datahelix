package com.scottlogic.deg.generator.constraints.atomic;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.inputs.validation.ProfileVisitor;
import com.scottlogic.deg.generator.inputs.validation.VisitableProfileElement;
import com.scottlogic.deg.generator.inputs.RuleInformation;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class IsStringShorterThanConstraint implements AtomicConstraint, VisitableProfileElement {
    public final Field field;
    private final Set<RuleInformation> rules;
    public final int referenceValue;
    private final boolean isSoftConstraint;

    public IsStringShorterThanConstraint(Field field, int referenceValue, Set<RuleInformation> rules) {
        this(field, referenceValue, rules, false);
    }

    private IsStringShorterThanConstraint(Field field, int referenceValue, Set<RuleInformation> rules, boolean isSoftConstraint) {
        if (referenceValue < 0){
            throw new IllegalArgumentException("Cannot create an IsStringShorterThanConstraint for field '" +
                field.name + "' with a a negative length.");
        }

        this.referenceValue = referenceValue;
        this.field = field;
        this.rules = rules;
        this.isSoftConstraint = isSoftConstraint;
    }

    public static IsStringShorterThanConstraint softConstraint(Field field, int referenceValue){
        return new IsStringShorterThanConstraint(field, referenceValue, Collections.emptySet(), true);
    }

    @Override
    public boolean isSoftConstraint() {
        return isSoftConstraint;
    }

    @Override
    public String toDotLabel(){
        return String.format("%s length < %s", field.name, referenceValue);
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o instanceof ViolatedAtomicConstraint) {
            return o.equals(this);
        }
        if (o == null || getClass() != o.getClass()) return false;
        IsStringShorterThanConstraint constraint = (IsStringShorterThanConstraint) o;
        return Objects.equals(field, constraint.field) && Objects.equals(referenceValue, constraint.referenceValue);
    }

    @Override
    public int hashCode(){
        return Objects.hash(field, referenceValue);
    }

    @Override
    public String toString() { return String.format("`%s` length < %d", field.name, referenceValue); }

    @Override
    public void accept(ProfileVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Set<RuleInformation> getRules() {
        return rules;
    }

    @Override
    public AtomicConstraint withRules(Set<RuleInformation> rules) {
        return new IsStringShorterThanConstraint(this.field, this.referenceValue, rules);
    }
}
