/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.generator.fieldspecs;

import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedSet;
import com.scottlogic.deg.generator.fieldspecs.whitelist.WeightedElement;
import com.scottlogic.deg.generator.fieldspecs.whitelist.FrequencyDistributedSet;
import com.scottlogic.deg.generator.restrictions.*;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Returns a FieldSpec that permits only data permitted by all of its inputs
 */
public class FieldSpecMerger {
    private static final RestrictionMergeOperation initialMergeOperation = new TypesRestrictionMergeOperation();
    private static final RestrictionMergeOperation[] mergeOperations = new RestrictionMergeOperation[]{
        initialMergeOperation,
        new StringRestrictionsMergeOperation(),
        new NumericRestrictionsMergeOperation(new NumericRestrictionsMerger()),
        new DateTimeRestrictionsMergeOperation(new DateTimeRestrictionsMerger()),
        new BlacklistRestictionsMergeOperation()
    };

    /**
     * Null parameters are permitted, and are synonymous with an empty FieldSpec
     * <p>
     * Returning an empty Optional conveys that the fields were unmergeable.
     */
    public Optional<FieldSpec> merge(FieldSpec left, FieldSpec right) {
        if (hasSet(left) && hasSet(right)) {
            return mergeSets(left, right);
        }
        if (hasSet(left)) {
            return combineSetWithRestrictions(left, right);
        }
        if (hasSet(right)) {
            return combineSetWithRestrictions(right, left);
        }
        return combineRestrictions(left, right);
    }

    private static WeightedElement<Object> mergeElements(WeightedElement<Object> left,
                                                         WeightedElement<Object> right) {
        return new WeightedElement<>(left.element(), left.weight() + right.weight());
    }

    private Optional<FieldSpec> mergeSets(FieldSpec left, FieldSpec right) {
        DistributedSet<Object> set = new FrequencyDistributedSet<>(left.getWhitelist().distributedSet().stream()
            .flatMap(leftHolder -> right.getWhitelist().distributedSet().stream()
                .filter(rightHolder -> elementsEqual(leftHolder, rightHolder))
                .map(rightHolder -> mergeElements(leftHolder, rightHolder)))
            .collect(Collectors.toSet()));

        return addNullable(left, right, setRestriction(set));
    }

    private static <T> boolean elementsEqual(WeightedElement<T> left, WeightedElement<T> right) {
        return left.element().equals(right.element());
    }

    private Optional<FieldSpec> combineSetWithRestrictions(FieldSpec set, FieldSpec restrictions) {
        DistributedSet<Object> newSet = new FrequencyDistributedSet<>(
            set.getWhitelist().distributedSet().stream()
                .filter(holder -> restrictions.permits(holder.element()))
                .collect(Collectors.toSet()));

        return addNullable(set, restrictions, setRestriction(newSet));
    }

    private Optional<FieldSpec> addNullable(FieldSpec left, FieldSpec right, FieldSpec newFieldSpec) {
        newFieldSpec = addFormatting(left, right, newFieldSpec);

        if (isNullable(left, right)) {
            return Optional.of(newFieldSpec);
        }

        if (noAllowedValues(newFieldSpec)) {
            return Optional.empty();
        }

        return Optional.of(newFieldSpec.withNotNull());
    }

    private boolean noAllowedValues(FieldSpec fieldSpec) {
        return (fieldSpec.getWhitelist() != null && fieldSpec.getWhitelist().set().isEmpty());
    }

    private FieldSpec setRestriction(DistributedSet<Object> set) {
        return FieldSpec.Empty.withWhitelist(set);
    }

    private boolean hasSet(FieldSpec fieldSpec) {
        return fieldSpec.getWhitelist() != null;
    }

    private boolean isNullable(FieldSpec left, FieldSpec right) {
        return left.isNullable() && right.isNullable();
    }

    private FieldSpec addFormatting(FieldSpec left, FieldSpec right, FieldSpec newFieldSpec) {
        if (left.getFormatting() != null) {
            return newFieldSpec.withFormatting(left.getFormatting());
        }
        if (right.getFormatting() != null) {
            return newFieldSpec.withFormatting(right.getFormatting());
        }
        return newFieldSpec;
    }

    private Optional<FieldSpec> combineRestrictions(FieldSpec left, FieldSpec right) {
        FieldSpec merging = FieldSpec.Empty;

        for (RestrictionMergeOperation operation : mergeOperations) {
            merging = operation.applyMergeOperation(left, right, merging);
        }

        return addNullable(left, right, merging);
    }
}
