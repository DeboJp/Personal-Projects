import tkinter as tk

# Updates the display whenever a button is clicked
def on_button_click(character):
    if character == 'C':
        # Clear the display if 'C' is clicked
        display.delete(0, tk.END)
    elif character == '=':
        # If '=' is clicked, evaluates and shows the result
        try:
            result = eval(display.get())
            display.delete(0, tk.END)
            display.insert(tk.END, str(result))
        except Exception as e:
            display.delete(0, tk.END)
            display.insert(tk.END, "Error")
    else:
        # For any other button, add its character to the display
        display.insert(tk.END, character)

# Create the main window - think of it as the frame for your painting
root = tk.Tk()
root.title("Simple_GUI_Calculator")  # Giving our calculator a title

# Visuals for display
display = tk.Entry(root, font=("Arial", 18), borderwidth=5, relief="ridge")
display.grid(row=0, column=0, columnspan=4, sticky='nsew')

# Buttons and their positions
buttons = [
    ('7', 1, 0), ('8', 1, 1), ('9', 1, 2),
    ('4', 2, 0), ('5', 2, 1), ('6', 2, 2),
    ('1', 3, 0), ('2', 3, 1), ('3', 3, 2),
    ('0', 4, 1),
    ('+', 1, 3), ('-', 2, 3), ('*', 3, 3), ('/', 4, 3),
    ('=', 4, 2), ('C', 4, 0)
]

# Creating and placing each button
for (text, row, col) in buttons:
    button = tk.Button(root, text=text, font=("Arial", 18), command=lambda t=text: on_button_click(t))
    button.grid(row=row, column=col, sticky='nsew', padx=5, pady=5)

# Grid cells are now expandable
for i in range(5):
    root.grid_rowconfigure(i, weight=1)
    root.grid_columnconfigure(i, weight=1)

root.mainloop()
