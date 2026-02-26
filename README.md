# Indexed Store

The `poly-index` library is a lightweight Java utility providing simple in-memory indexing for objects, with multi-index lookup capabilities. It is basically a wrapper around standard java maps, but also allows Lucene-based in-memory indices. This allows the retrieval of objects using various indexing keys without manually managing multiple maps.

User interaction is straightforward: you "push" objects into the store where they are automatically indexed according to predefined keys. You can then retrieve them by querying the store with a specific key and a matching value, or even a list of keys to implement fallback or priority-based lookup logic.

## I. Installation

Add the following in your `pom.xml`:

```xml
<dependency>
    <groupId>tech.illuin</groupId>
    <artifactId>poly-index</artifactId>
    <version>0.9.1</version>
</dependency>
```

## II. Quick API Overview

Simple example: exact match

```java
// Define a key based on a field
Key<User> EMAIL_KEY = Key.of(User::email);

// Initialize store and push data
IndexedStore<User> store = new MapStore<>(Index.of(EMAIL_KEY));
store.push(new User("john.doe@example.com", "John Doe"));

// Retrieve by value
Optional<User> user = store.getFirst("john.doe@example.com", EMAIL_KEY);
```

More complex example: priority matching with multiple keys

```java
// Define multiple keys (exact match and combination)
Key<Vehicle> PLATE_KEY = Key.of(Vehicle::plate);
Key<Vehicle> BRAND_MODEL_KEY = MapCombinationKey.of(
    MapCombinationKey.requires(Vehicle::brand, Vehicle::model),
    MapIndexType.FIRST
);

IndexedStore<Vehicle> store = new MapStore<>(Index.of(PLATE_KEY, BRAND_MODEL_KEY));
store.pushAll(vehicles);

// Query using the "exemplar pattern" ; try each key in order until a match is found
Vehicle search = new Vehicle("ABC-123", "Toyota", "Corolla");
Optional<Vehicle> match = store.getFirstMatch(search, List.of(PLATE_KEY, BRAND_MODEL_KEY));
```

## III. Workflow

The general workflow when using `poly-index` is as follows:

1. **Define index keys**: specify how identifiers are extracted from your objects
  * Use `Key.of(Function)` for simple field matching.
  * Use `MapCombinationKey.of(...)` for composite keys (multiple fields).
2. **Initialize the store**: Create an `IndexedStore` by providing it with an `Index` registry containing all the keys you intend to use.
3. **Populate the store**: Use `push(T)` or `pushAll(Collection<T>)` to add objects. They will be automatically indexed across all registered keys.
4. **Query the store**:
 * `get(value, key)`: returns all matches for a given key and value.
 * `getFirst(value, key)`: returns the first match for a given key and value.
 * `getFirstMatch(exemplar, keys)`: (exemplar pattern) takes a template object and a list of keys, trying each key in order.

## IV. Key definitions & patterns

The `poly-index` library provides several ways to define how your objects are indexed, ranging from simple field lookups to more complex composite and multi-valued keys.

### 1. Simple Keys

Simple keys are best for exact matching on single fields or simple transformations.

```java
// Simple field-based key
Key<User> EMAIL_KEY = Key.of(User::email);

// Named key (useful for debugging and custom indexing logic)
Key<User> NAME_KEY = Key.of("user-name", User::name);

// Key with a custom transformation
Key<Product> BRAND_MODEL_KEY = Key.of(p -> p.brand() + ":" + p.model());

// Key with specific MapIndexType (stores all matches for the same key)
Key<Product> CAT_KEY = Key.of(Product::category, MapIndexType.ALL);
```

### 2. MapCombinationKey (composite indices)

`MapCombinationKey` is the primary way to define indices based on multiple fields. It can accept a `requires` clause (fields that must be non-null) and/or `excludes` clause (fields must be null).

Basic composite key:

```java
// Matches ONLY if both brand and model are present
public static final Key<Vehicle> BRAND_MODEL_KEY = MapCombinationKey.of(
    requires(Vehicle::brand, Vehicle::model),
    MapIndexType.FIRST
);
```

Using requirements and exclusions:

```java
public record Product(String category, String subCategory, String sku, String tag) {
    /* Matches ONLY if category, subCategory and sku are all present */
    public static final Key<Product> CAT_SUB_SKU_KEY = MapCombinationKey.of(
        requires(Product::category, Product::subCategory, Product::sku),
        MapIndexType.FIRST
    );

    /* Matches if category and subCategory are present, but ONLY if sku is NULL */
    public static final Key<Product> CAT_SUB_KEY = MapCombinationKey.of(
        requires(Product::category, Product::subCategory),
        excludes(Product::sku),
        MapIndexType.FIRST
    );
}
```

A single key can support multiple "variants" of indexing requirements.

```java
// A key that matches EITHER (brand + model) OR (brand + licensePlate)
Key<Vehicle> MULTI_VARIANT_KEY = MapCombinationKey.of(List.of(
    MapCombinationKey.variant(requires(Vehicle::brand, Vehicle::model)),
    MapCombinationKey.variant(requires(Vehicle::brand, Vehicle::licensePlate))
), MapIndexType.FIRST);
```

### 3. MapIndexType & Strategies

When indexing multiple objects that might share the same key value, `MapIndexType` (or custom `MapIndexStrategy`) defines which ones are kept:

* `ALL`: (default) stores all objects matching the key in a list.
* `FIRST`: only stores the first object encountered for a given key value. Subsequent objects are ignored.
* `LAST`: only stores the last object encountered. Each new object replaces the previous one for that key.

### 4. Handling multi-valued Keys

For scenarios like matching against a list of tags or variants, use custom functions returning an `IndexKeyCollection`.

```java
public static final Key<Product> TAG_KEY = MapCombinationKey.of(
    requires(Product::category),
    new IndexFirstStrategy<>(entity -> {
        // Generate multiple index entries from a single field
        List<String> tags = Arrays.asList(entity.tag().split(","));
        return IndexKeyCollection.of(tags);
    })
);
```

### 5. Lucene-based Indexing (fuzzy & prefix-based lookup)

For more advanced search requirements like fuzzy matching or prefix-based lookups, you can use Lucene-based keys. These leverage an in-memory Lucene index while maintaining the same `IndexedStore` API.

```java
// Fuzzy match: matches "Doe" even if searched as "Doo"
Key<Person> FUZZY_KEY = Key.ofLucene("fuzzy", Person::lastName, new FuzzyMatchStrategy());

// Prefix match: matches "Doe" if searched as "Do"
Key<Person> PREFIX_KEY = Key.ofLucene("prefix", Person::lastName, new PartialMatchStrategy());

// Querying remains consistent with map-based stores
Optional<Person> p = store.getFirst("Doo", FUZZY_KEY);
```

### 6. Advanced Lucene Queries

`Key.ofLuceneQuery` provides full control over how Lucene `Document`s are created and how `Query` objects are parsed.

```java
public static final Key<Person> ADVANCED_KEY = Key.ofLuceneQuery(
    person -> List.of(
        new StringField("fname", person.firstName(), Field.Store.YES),
        new StringField("lname", person.lastName(), Field.Store.YES)
    ),
    (parser, criteria) -> parser.parse(
        "fname:" + criteria.firstName() + " AND lname:" + criteria.lastName()
    )
);
```

## IV. Dev Installation

This project will require you to have the following:

* Java 17+
* Git (versioning)
* Maven (dependency resolving, publishing and packaging) 
