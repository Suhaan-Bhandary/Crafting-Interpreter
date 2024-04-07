import os
import sys

def defineType(file, base_name, class_name, fields):
    file.write("    static class " + class_name + " extends " + base_name + " {\n");
    file.write("    " + class_name + "(" + fields + ") {\n");

    # define the parameters
    fields_list = fields.split(", ");
    for field in fields_list:
        name = field.split()[1]
        file.write("        this." + name + " = " + name + ";\n")
    file.write("    }\n");

    file.write(f"""

    @Override
    <R> R accept(Visitor<R> visitor) {{
        return visitor.visit{class_name}{base_name}(this);
    }}

    """)

    # Define fields
    for field in fields_list:
        file.write("    final " + field + ";\n");

    file.write("  }\n");

def defineVisitor(f, base_name, types):
    f.write("   interface Visitor<R> {\n")

    for type in types:
        type_name = type.split(":")[0].strip()
        f.write("   R visit"+ type_name + base_name + "(" + type_name + " " + base_name.lower() +");\n")

    f.write("   }\n")


def defineAst(output_dir, base_name, types):
    path = os.path.join(output_dir, base_name + '.java')

    with open(path, 'w') as f:
        f.write("package com.craftinginterpreters.lox;\n")
        f.write("\n")
        f.write("import java.util.List;\n")
        f.write("\n")
        f.write("abstract class " + base_name + " {\n")

        defineVisitor(f, base_name, types)

        # Define the static classes inside
        for type in types:
            class_name = type.split(":")[0].strip()
            fields = type.split(":")[1].strip()
            defineType(f, base_name, class_name, fields)

        f.write("\n")
        f.write("   abstract <R> R accept(Visitor<R> visitor);")

        f.write("}\n")


def main(argv):
    if len(argv) != 2:
        print("Usage: python GenerateAst.py <output directory>")
        return

    output_dir = argv[1]
    if(not os.path.exists(output_dir)):
        print("Invalid output directory in argv")
        return

    types = [
        "Assign   : Token name, Expr value",
        "Binary    : Expr left, Token operator, Expr right",
        "Grouping  : Expr expression",
        "Literal   : Object value",
        "Unary     : Token operator, Expr right",
        "Variable  : Token name",
    ]

    defineAst(output_dir, "Expr", types);
    defineAst(output_dir, "Stmt", [
      "Block : List<Stmt> statements",
      "Expression : Expr expression",
      "Print      : Expr expression",
      "Var        : Token name, Expr initializer"
    ]);

if __name__ == "__main__":
    main(sys.argv)
