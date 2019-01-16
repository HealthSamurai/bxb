# bxb

bidirectional transformations approach


## Motivation

We are interested in bidirectional transformations formalizm and implementation for
hierarchiecal structures (json-like). Transformation without loosing information is 
important aspect of semantic integrations between systems, as well flexibility in 
representation is useful in same systzm for different usecases.

Here is a wish-list of properties and features:

* declarative and expressive as possible
* non-turing complete, which allow to analyze transformations and prove it
* practical and non-reversable extensions
* debug frendly
* inference transformation by samples
* efficient implementation for production usage
* string extension to parse/transform strings



## Formalizm

Object of transformation is hierarchiecal data structures, which
are a composition of:

* primitives (like string, number etc)
* map (set of key value pairs)
* set (unordered uniq set of values)
* collection (ordered; non uniq collection of values)

### Canonical form

There is bidirectional operation to transofm such data 
structures into a set of pairs - path & value:


```yaml
name:
  - given: ['s', 's']
    family: 's'
birthDate: '1980'

# canonical representation

name.0.given.0  's'
name.0.given.1  's'
birthDate       '1980'
```

### Transformation flow

```
data structure 
  => canonical form 
    => transformation 
       => canonical form 
         => result data structure
```

Transformation is a collection of transform operations over cannonical form.
This operations are applied in order to source to get target result.
To do backward transformation operations are applied to result data structure
in reverse order.

Transform operation is a function from set of path/value pairs 
into path/value pairs:

T(pvs; prog)=> pvs

### Singular transformation

transform source path/value pair into target path/value and backward

### Basic transformation

In special case transformation consist of two independent  transformations

* source path => target path
* source value => target value

For example:

```yaml
path: name.given => first_name
value: prefix/unfix  pre, s => "${pre}_{s}" and back "${pre}_{s}" => s
```

Each transformation operation (path and value) are two way function (i.e. two functions)

target <- op params -> source

### Parametrized transformation

generalizing basic transformation transormation can describe a 
set of transformations

One of special case is parametrized path's. For example

```yaml
name.#.given => name.#.first_name
```
Where `#` is a place holder, which in concrete transformation will get
a specific value, becomming a parameter passed into resulting path.

Example:

```yaml
name.{}.given => ({} = ?) => name.{}.first_name

name.{}.given => ({} = 0) => name.0.first_name
name.{}.given => ({} = 1) => name.z.first_name
```

### Value Dependent transformation

In more general case, path transformation can depend on value and value transformation can depend on path.
In simple words data can move to structure and backward, consider this:

```yaml
- system: phone
  value: <phone-1>
- system: phone
  value: <phone-2>
- system: mail
  value: <mail>

#=>

phone: [<phone-1>, <phone-2>]
mail: [<mail>]

```

###  Dependent transformation

In more general case transformation is map set of path/value pairs to set of path/value pairs
i.e. one path/value pair transformation depends on another

TODO: investigate reversability properties


## Abstract machine

Bx programm looks like list of transformations:

```yaml
t1: tr-type params
t2: tr-type params
...

```

To transform forward data structure represented in
canonical form; and operations applied one by one; where result of previous
operation is input for next one.
Backward transformation is apply same rules in reverse mode in reverse order.

pseudo code:

```py
direction = forward or backward
data = canonical(input)
operations = if forward then operations else reverse(operations)
for op in operations
   data = APPLY( data, op, direction)
return data
```


## Path transformations

path transforms is

* match source path pattern (get params values)
* apply params valus to target path

Parameters can be anonymous with `{}`; or indexed `{idx}` or named `{name}`

```yaml
# rename field

attr1 : attr2

{attr1: <v1>, prop: <v2>} => {attr2: <v1>, prop: <v2>}

# change depth

first_name :  name.given
last_name  :  name.family


# parametrized

name.{} : {}

swap.{1}.{2} : swapped.{2}.{1}

contact.{use}.{system} : swapped.{system}.{use}

```
There is special name for value to use in path transformation {%%} and bidirectional function can be applied to it or other params

```yaml

path.path : path.path.{func(%%)}

path.% : path.{prefix('pre_', %)}
# if we reverse it
path.% : path.{un-prefix('pre_', %)}

```


## Parametrized Path Patterns

Element:     (path, value, metadata)

Path:        [item item ...], where item = key | index | expression

Value:       primitive | map | collection | set

Transform:   source-path target-path ; expressions | value transform


```
telecom.(i) <=> (sys) | sys (get % :system) | (assoc % :ord i)

contact.(sys).(use) <=> telecom | (map :value % :system sys :use use)

```
