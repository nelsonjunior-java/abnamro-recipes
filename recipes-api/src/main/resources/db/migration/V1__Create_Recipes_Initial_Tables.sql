-- V1__Create_Recipes_Tables.sql

-- Create the ingredients table
CREATE TABLE ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create the recipes table
CREATE TABLE recipes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_vegetarian BOOLEAN NOT NULL,
    servings INTEGER NOT NULL,
    instructions TEXT NOT NULL,
    instructions_tsv tsvector  -- Add a tsvector column for optimized full-text search
);

-- Create the join table to handle the many-to-many relationship between recipes and ingredients
CREATE TABLE recipe_ingredients (
    recipe_id INTEGER NOT NULL,
    ingredient_id INTEGER NOT NULL,
    PRIMARY KEY (recipe_id, ingredient_id),
    FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE CASCADE
);

-- Create a trigger to update the tsvector column whenever the instructions column changes
CREATE FUNCTION update_instructions_tsv() RETURNS trigger AS $$
BEGIN
  NEW.instructions_tsv := to_tsvector('english', NEW.instructions);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER instructions_tsv_update BEFORE INSERT OR UPDATE
ON recipes FOR EACH ROW EXECUTE FUNCTION update_instructions_tsv();

-- Index to improve full-text search within instructions
CREATE INDEX idx_instructions_tsv ON recipes USING gin(instructions_tsv);
