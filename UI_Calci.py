import tkinter as tk
from tkinter import font, scrolledtext
import math


class Calculator:
    def __init__(self, root):
        self.root = root
        self.root.title("Scientific Calculator")
        self.root.geometry("700x700")
        self.root.resizable(False, False)
        self.root.configure(bg="#2c3e50")

        # Variables to store calculation state
        self.expression = ""
        self.previous_expression = ""
        self.display_value = "0"
        self.new_number = False  # Track if we just pressed an operator
        self.history = []  # Store all calculations

        # Create main container with history and calculator
        self.create_main_layout()

        # Bind keyboard events
        self.root.bind('<Key>', self.on_key_press)

    def create_main_layout(self):
        """Create main layout with history and calculator side by side"""
        # Main container
        main_frame = tk.Frame(self.root, bg="#2c3e50")
        main_frame.pack(fill=tk.BOTH, expand=True)

        # Left side - History panel
        history_frame = tk.Frame(main_frame, bg="#34495e", width=200)
        history_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=False, padx=5, pady=10)
        history_frame.pack_propagate(False)

        history_label = tk.Label(
            history_frame,
            text="History",
            font=("Arial", 12, "bold"),
            bg="#34495e",
            fg="#ecf0f1"
        )
        history_label.pack(pady=5)

        # Scrolled text widget for history
        self.history_text = scrolledtext.ScrolledText(
            history_frame,
            width=25,
            height=35,
            bg="#2c3e50",
            fg="#ecf0f1",
            font=("Arial", 10),
            state=tk.DISABLED
        )
        self.history_text.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)

        # Right side - Calculator
        calc_frame = tk.Frame(main_frame, bg="#2c3e50")
        calc_frame.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True, padx=10, pady=10)

        # Create display and buttons in calc_frame
        self.create_display(calc_frame)
        self.create_buttons(calc_frame)

    def create_display(self, parent):
        """Create the display screen for the calculator"""
        display_frame = tk.Frame(parent, bg="#34495e", height=100)
        display_frame.pack(fill=tk.BOTH, padx=10, pady=10)
        display_frame.pack_propagate(False)

        # Previous expression display
        self.previous_label = tk.Label(
            display_frame,
            text="",
            font=("Arial", 14),
            bg="#34495e",
            fg="#95a5a6",
            anchor="e"
        )
        self.previous_label.pack(fill=tk.BOTH, padx=10, pady=(5, 0))

        # Current expression display
        self.display = tk.Label(
            display_frame,
            text=self.display_value,
            font=("Arial", 32, "bold"),
            bg="#34495e",
            fg="#ecf0f1",
            anchor="e"
        )
        self.display.pack(fill=tk.BOTH, padx=10, pady=(0, 5))

    def create_buttons(self, parent):
        """Create all calculator buttons"""
        button_frame = tk.Frame(parent, bg="#2c3e50")
        button_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)

        # Button layout
        buttons = [
            [("C", self.clear, "#e74c3c"), ("←", self.backspace, "#e74c3c"),
             ("√", self.square_root, "#3498db"), ("÷", self.add_operator, "#f39c12")],

            [("7", self.add_digit, "#ecf0f1"), ("8", self.add_digit, "#ecf0f1"),
             ("9", self.add_digit, "#ecf0f1"), ("×", self.add_operator, "#f39c12")],

            [("4", self.add_digit, "#ecf0f1"), ("5", self.add_digit, "#ecf0f1"),
             ("6", self.add_digit, "#ecf0f1"), ("−", self.add_operator, "#f39c12")],

            [("1", self.add_digit, "#ecf0f1"), ("2", self.add_digit, "#ecf0f1"),
             ("3", self.add_digit, "#ecf0f1"), ("+", self.add_operator, "#f39c12")],

            [("0", self.add_digit, "#ecf0f1"), (".", self.add_digit, "#ecf0f1"),
             ("x²", self.square, "#3498db"), ("=", self.calculate, "#27ae60")],
        ]

        # Create button grid
        for row_idx, row in enumerate(buttons):
            for col_idx, (text, command, color) in enumerate(row):
                btn = tk.Button(
                    button_frame,
                    text=text,
                    font=("Arial", 18, "bold"),
                    bg=color,
                    fg="white" if color != "#ecf0f1" else "#2c3e50",
                    command=lambda t=text, cmd=command: cmd(t),
                    relief=tk.RAISED,
                    bd=2
                )
                btn.grid(row=row_idx, column=col_idx, sticky="nsew", padx=5, pady=5)

        # Configure grid weights for responsiveness
        for i in range(5):
            button_frame.grid_rowconfigure(i, weight=1)
        for i in range(4):
            button_frame.grid_columnconfigure(i, weight=1)

    def add_digit(self, digit):
        """Add a digit or decimal point to the expression"""
        # Prevent multiple decimal points in one number
        if digit == ".":
            current_number = self.expression.split()[-1] if self.expression else "0"
            if "." in current_number:
                return
            # Special case: if current number is just "-", treat as "-0."
            if current_number == "-":
                self.display_value = "-0."
                self.expression = self.expression[:-1] + "-0."
                self.update_display()
                return

        # If we just pressed an operator (or after =), start a new number
        if self.new_number:
            # Check if expression ends with an operator (space means operator was added)
            if self.expression and self.expression[-1] == " ":
                # We have an operator at the end, append the new digit
                if digit == ".":
                    self.display_value = "0."
                    self.expression += "0."
                else:
                    self.display_value = digit
                    self.expression += digit
            else:
                # No operator at end, start fresh (this handles after = or after negative sign)
                if digit == ".":
                    self.display_value = "0."
                    self.expression = "0."
                else:
                    self.display_value = digit
                    self.expression = digit
            self.new_number = False
        else:
            # Appending to existing number
            if self.display_value == "0" and digit != ".":
                self.display_value = digit
            elif self.display_value == "-" and digit != ".":
                # Adding first digit after negative sign
                self.display_value = "-" + digit
            else:
                self.display_value += digit
            self.expression += digit

        self.update_display()

    def add_operator(self, operator):
        """Add an operator to the expression"""
        # Convert display operators to standard notation
        operator_map = {"÷": "/", "×": "*", "−": "-"}
        actual_operator = operator_map.get(operator, operator)

        # Handle negative sign - only if expression is empty or ends with an operator (space)
        # This means "-" is a negative sign only:
        # 1. At the start (no expression), OR
        # 2. Right after another operator (expression ends with space like "5 + ")
        if actual_operator == "-" and (not self.expression or self.expression.endswith((" +", " -", " *", " /"))):
            # This is a minus sign for a negative number
            if not self.expression:
                self.display_value = "-"
                self.expression = "-"
            else:
                self.display_value = "-"
                self.expression += "-"
            self.new_number = False
            self.update_display()
            return

        # If we have a number ready to operate on
        if self.expression and not self.new_number:
            # Check if we have operators in the expression
            if any(op in self.expression for op in "+-*/"):
                # Auto-calculate the current expression
                try:
                    result = eval(self.expression)
                    result = round(result, 10)
                    self.add_to_history(self.expression, str(result))
                    self.expression = str(result)
                    self.display_value = str(result)
                except Exception as e:
                    pass

            # Now add the new operator
            self.expression += " " + actual_operator + " "
            self.previous_expression = self.expression
            self.display_value = "0"
            self.new_number = True
            self.update_display()

    def calculate(self, _=None):
        """Calculate the result of the expression"""
        try:
            if not self.expression:
                return

            # Check for 0/0 (invalid)
            if self.expression.strip() == "0 / 0":
                self.display_value = "Invalid"
                self.previous_expression = self.expression + " ="
                self.add_to_history(self.expression, "Invalid")
                self.expression = ""
                self.new_number = True
                self.update_display()
                return

            result = eval(self.expression)

            # Round to avoid floating point precision issues
            result = round(result, 10)

            self.previous_expression = self.expression + " ="
            self.add_to_history(self.expression, str(result))
            self.expression = str(result)
            self.display_value = str(result)
            self.new_number = True
            self.update_display()
        except ZeroDivisionError:
            self.display_value = "Error: Div by 0"
            self.previous_expression = self.expression + " ="
            self.add_to_history(self.expression, "Error: Div by 0")
            self.expression = ""
            self.new_number = True
            self.update_display()
        except Exception as e:
            self.display_value = "Error"
            self.expression = ""
            self.new_number = True
            self.update_display()

    def square(self, _=None):
        """Calculate the square of the current number"""
        try:
            if self.expression:
                result = eval(self.expression)
            else:
                result = float(self.display_value)

            result = result ** 2
            result = round(result, 10)

            calc_expr = f"({self.expression if self.expression else self.display_value})²"
            self.previous_expression = calc_expr
            self.add_to_history(calc_expr, str(result))
            self.expression = str(result)
            self.display_value = str(result)
            self.new_number = True
            self.update_display()
        except Exception as e:
            self.display_value = "Error"
            self.expression = ""
            self.new_number = True
            self.update_display()

    def square_root(self, _=None):
        """Calculate the square root of the current number"""
        try:
            if self.expression:
                result = eval(self.expression)
            else:
                result = float(self.display_value)

            if result < 0:
                self.display_value = "Error: Negative num"
                self.new_number = True
                self.update_display()
                return

            result = math.sqrt(result)
            result = round(result, 10)

            calc_expr = f"√({self.expression if self.expression else self.display_value})"
            self.previous_expression = calc_expr
            self.add_to_history(calc_expr, str(result))
            self.expression = str(result)
            self.display_value = str(result)
            self.new_number = True
            self.update_display()
        except Exception as e:
            self.display_value = "Error"
            self.expression = ""
            self.new_number = True
            self.update_display()

    def clear(self, _=None):
        """Clear all calculations"""
        self.expression = ""
        self.previous_expression = ""
        self.display_value = "0"
        self.new_number = False
        self.history = []
        self.display_history()
        self.update_display()

    def backspace(self, _=None):
        """Remove the last character from the expression"""
        if self.display_value != "0":
            self.display_value = self.display_value[:-1]
            if not self.display_value:
                self.display_value = "0"
            self.expression = self.expression[:-1]
            self.update_display()

    def update_display(self):
        """Update the display with current values"""
        self.previous_label.config(text=self.previous_expression)
        self.display.config(text=self.display_value)

    def add_to_history(self, expression, result):
        """Add calculation to history"""
        self.history.append(f"{expression} = {result}")
        self.display_history()

    def display_history(self):
        """Display all history in the history text widget"""
        self.history_text.config(state=tk.NORMAL)
        self.history_text.delete(1.0, tk.END)

        for item in self.history:
            self.history_text.insert(tk.END, item + "\n")

        # Scroll to the bottom
        self.history_text.see(tk.END)
        self.history_text.config(state=tk.DISABLED)

    def on_key_press(self, event):
        """Handle keyboard input"""
        key = event.char

        # Numbers
        if key in '0123456789':
            self.add_digit(key)
        # Decimal point
        elif key == '.':
            self.add_digit('.')
        # Operators
        elif key == '+':
            self.add_operator('+')
        elif key == '-':
            self.add_operator('−')  # Use display character
        elif key == '*':
            self.add_operator('×')  # Use display character
        elif key == '/':
            self.add_operator('÷')  # Use display character
        # Enter or equals
        elif key in '\r=' or event.keysym == 'Return':
            self.calculate()
        # Backspace
        elif event.keysym == 'BackSpace':
            self.backspace()
        # Clear with 'c' or 'C'
        elif key.upper() == 'C':
            self.clear()


def main():
    root = tk.Tk()
    calculator = Calculator(root)
    root.mainloop()


if __name__ == "__main__":
    main()
