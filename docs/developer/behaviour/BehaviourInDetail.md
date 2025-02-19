# Behaviour in Detail
## Nullness
### Behaviour
Nulls can always be produced for a field, except when a field is explicitly not null. 

### Misleading Examples
|Field is               |Null produced|
|:----------------------|:-----------:|
|Of type X              | ✔ |
|Not of type X          | ✔ |
|In set [X, Y, ...]     | ✔ |
|Not in set [X, Y, ...] | ✔ |
|Equal to X             | ❌ |
|Not equal to X         | ✔ |
|Greater than X         | ✔ |
|Null                   | ✔ |
|Not null               | ❌ |

For the profile snippet:
```
{ "if":
    { "field": "A", "is": "equalTo", "value": 1 },
  "then":
    { "field": "B", "is": "equalTo", "value": 2 }
},
{ "field": "A", "is": "equalTo", "value": 1 }
```

|Allowed value of A|Allowed value of B|
|------------------|------------------|
|1                 |2                 |

## Type Implication
### Behaviour
No operators imply type (except ofType ones). By default, all values are allowed.

### Misleading Examples
Field is greater than number X:

|Values                |Can be produced|
|----------------------|:-------------:|
|Numbers greater than X|✔ |
|Numbers less than X   |❌ |
|Null                  |✔ |
|Strings               |✔ |
|Date-times            |✔ |

## Violation of Rules
### Behaviour
Rules, constraints and top level `allOf`s are all equivalent in normal generation.

In violation mode (an _alpha feature_), rules are treated as blocks of violation.
For each rule, a file is generated containing data that can be generated by combining the
violation of that rule with the non-violated other rules.

This is equivalent to the behaviour for constraints and `allOf`s, but just splitting it
into different files.

## General Strategy for Violation
_This is an alpha feature. Please do not rely on it. If you find issues with it, please [report them](https://github.com/finos/datahelix/issues)._ 
### Behaviour
The violation output is not guaranteed to be able to produce any data,
even when the negation of the entire profile could produce data.

### Why
The violation output could have been calculated by simply negating an entire rule or profile. This could then produce all data that breaks the original profile in any way. However, this includes data that breaks the data in multiple ways at once. This could be very noisy, because the user is expected to test one small breakage in a localised area at a time.

To minimise the noise in an efficient way, a guarantee of completeness is broken. The system does not guarantee to be able to produce violating data in all cases where there could be data which meets this requirement. In some cases this means that no data is produced at all.

In normal negation, negating `allOf [A, B, C]` gives any of the following:
1) `allOf[NOT(A), B, C]`
2) `allOf[A, NOT(B), C]`
3) `allOf[A, B, NOT(C)]`
4) `allOf[NOT(A), NOT(B), C]`
5) `allOf[A, NOT(B), NOT(C)]`
6) `allOf[NOT(A), B, NOT(C)]`
7) `allOf[NOT(A), NOT(B), NOT(C)]`

These are listed from the least to most noisy. The current system only tries to generate data by negating one sub-constraint at a time (in this case, producing only 1, 2 and 3).

### Misleading examples
When a field is a string, an integer and not null, no data can be produced normally,
but data can be produced in violation mode.

|Values                |Can be produced when in violation mode|
|----------------------|:-------------------------------------|
|Null                  |✔ (By violating the "not null" constraint) |
|Numbers               |✔ (By violating the "string" constraint) |
|Strings               |✔ (By violating the "integer" constraint) |
|Date-times            |❌ (This would need both the "string" and "integer" constraints to be violated at the same time) |

If a field is set to null twice, no data can be produced in violation mode because it tries to evaluate null and not null:

|Values                |Can be produced when in violation mode|
|----------------------|:-------------------------------------|
|Null                  |❌|
|Numbers               |❌|
|Strings               |❌|
|Date-times            |❌|

