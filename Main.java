import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import javax.swing.*;

public class Main {
    private final JFrame frame;
    private final JTextArea displayArea;
    private final JTextArea historyArea;
    private StringBuilder expression = new StringBuilder();

    public Main() {
        frame = new JFrame("JAVASCRIPT CALCULATOR");
        frame.setBackground(Color.black);
        displayArea = new JTextArea(3, 18);
        historyArea = new JTextArea(10, 18);
        historyArea.setBackground(Color.black);
        historyArea.setForeground(Color.white);
        buildUi();
    }

    private void buildUi() {
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Agency fb", Font.BOLD, 24));
        displayArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        displayArea.setAlignmentY(Component.CENTER_ALIGNMENT);
        displayArea.setBackground(Color.black);
        displayArea.setForeground(Color.white);
        displayArea.setFocusable(true);

        historyArea.setEditable(false);
        historyArea.setFont(new Font("Agency fb", Font.PLAIN, 18));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);

        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("History"));

        JPanel buttonPanel = createButtonPanel();

        frame.setLayout(new BorderLayout(8, 8));
        frame.add(displayArea, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(historyScroll, BorderLayout.EAST);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(true);
        frame.setSize(600,500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        attachKeyboardBindings();
        frame.getRootPane().setFocusable(true);
        SwingUtilities.invokeLater(() -> frame.getRootPane().requestFocusInWindow());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                frame.getRootPane().requestFocusInWindow();
            }
        });
        updateDisplay();
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 4, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(Color.black);
        panel.setForeground(Color.white);

        String[] buttons = {
            "C", "←", "π", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "±", "="
        };

        for (String label : buttons) {
            JButton button = new JButton(label);
            button.setFont(new Font("Agency fb", Font.PLAIN, 20));
            button.setBackground(Color.black);
            button.setForeground(Color.white);  
            button.setFocusable(false);
            button.addActionListener(e -> handleButton(label));
            panel.add(button);
        }

        JPanel extraPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        JButton square = new JButton("x²");
        JButton root = new JButton("√x");
        square.setFont(new Font("Agency fb", Font.PLAIN, 20));
        root.setFont(new Font("Agency fb", Font.PLAIN, 20));
        square.setFocusable(false);
        square.setBackground(Color.black);
        square.setForeground(Color.white);  
        root.setFocusable(false);
        root.setBackground(Color.black);        
        root.setForeground(Color.white);
        square.addActionListener(e -> applyUnaryOperation("square"));
        root.addActionListener(e -> applyUnaryOperation("sqrt"));
        extraPanel.add(square);
        extraPanel.add(root);
        extraPanel.setBackground(Color.black);

        JPanel wrapper = new JPanel(new BorderLayout(6, 6));
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(extraPanel, BorderLayout.SOUTH);
        wrapper.setBackground(Color.black);

        return wrapper;
    }

    private void handleButton(String label) {
        switch (label) {
            case "C" -> clearAll(); 
            case "←" -> backspace();
            case "=" -> calculateResult();
            case "π" -> appendInput("pi");
            case "±" -> toggleSign();
            case "+", "-", "*", "/" -> appendOperator(label);
            case "." -> appendDecimalPoint();
            default -> appendInput(label);
        }
    }

    private void clearAll() {
        expression.setLength(0);
        updateDisplay();
    }

    private void backspace() {
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
            updateDisplay();
        }
    }

    private void appendInput(String value) {
        if (value.equals("pi")) {
            expression.append("pi");
        } else {
            expression.append(value);
        }
        updateDisplay();
    }

    private void appendOperator(String operator) {
        if (expression.isEmpty()) {
            if (operator.equals("-")) {
                expression.append(operator);
            }
            return;
        }
        char last = expression.charAt(expression.length() - 1);
        if (last == '+' || last == '-' || last == '*' || last == '/' || last == '.') {
            expression.setCharAt(expression.length() - 1, operator.charAt(0));
        } else {
            expression.append(operator);
        }
        updateDisplay();
    }

    private void appendDecimalPoint() {
        if (expression.isEmpty() || isOperator(expression.charAt(expression.length() - 1))) {
            expression.append("0.");
            updateDisplay();
            return;
        }
        int i = expression.length() - 1;
        while (i >= 0 && !isOperator(expression.charAt(i))) {
            if (expression.charAt(i) == '.') {
                return;
            }
            i--;
        }
        expression.append('.');
        updateDisplay();
    }

    private void toggleSign() {
        if (expression.isEmpty()) {
            expression.append("-");
            updateDisplay();
            return;
        }

        int lastOp = lastOperatorPosition();
        if (lastOp == -1) {
            if (expression.charAt(0) == '-') {
                expression.deleteCharAt(0);
            } else {
                expression.insert(0, '-');
            }
        } else {
            if (expression.charAt(lastOp + 1) == '-') {
                expression.deleteCharAt(lastOp + 1);
            } else {
                expression.insert(lastOp + 1, '-');
            }
        }
        updateDisplay();
    }

    private int lastOperatorPosition() {
        for (int i = expression.length() - 1; i >= 0; i--) {
            char ch = expression.charAt(i);
            if (isOperator(ch) && ch != '-' && !(i == 0 && ch == '-')) {
                return i;
            }
        }
        return -1;
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private void applyUnaryOperation(String operation) {
        if (expression.isEmpty()) {
            return;
        }
        try {
            double value = evaluateExpression(expression.toString());
            double result;
            if (operation.equals("square")) {
                result = value * value;
                addHistory("square(" + formatExpression(expression.toString()) + ")", result);
            } else {
                if (value < 0) {
                    showError("Invalid input for sqrt");
                    return;
                }
                result = Math.sqrt(value);
                addHistory("sqrt(" + formatExpression(expression.toString()) + ")", result);
            }
            expression.setLength(0);
            expression.append(formatResult(result));
            updateDisplay();
        } catch (Exception ex) {
            showError("Error");
        }
    }

    private void calculateResult() {
        if (expression.isEmpty()) {
            return;
        }
        String expr = expression.toString();
        try {
            double result = evaluateExpression(expr);
            if (Double.isFinite(result)) {
                addHistory(expr, result);
                expression.setLength(0);
                expression.append(formatResult(result));
                updateDisplay();
            } else {
                addHistory(expr, Double.NaN);
                showError("Undefined");
            }
        } catch (IllegalArgumentException e) {
            showError("Invalid Expression");
        } catch (Exception e) {
            showError("Error");
        }
    }

    private void showError(String message) {
        displayArea.setText(message);
        expression.setLength(0);
    }

    private void addHistory(String expr, double result) {
        if (Double.isFinite(result)) {
            historyArea.append(expr + " = " + formatResult(result) + "\n");
        } else {
            historyArea.append(expr + " = Undefined\n");
        }
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    private String formatResult(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        DecimalFormat format = new DecimalFormat("0.##########");
        return format.format(value);
    }

    private String formatExpression(String expr) {
        return expr.replace("pi", String.valueOf(Math.PI));
    }

    private void updateDisplay() {
        displayArea.setText(expression.length() == 0 ? "0" : expression.toString());
    }

    private void attachKeyboardBindings() {
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        bindKey(inputMap, actionMap, KeyEvent.VK_0, 0, "type-0", "0");
        bindKey(inputMap, actionMap, KeyEvent.VK_1, 0, "type-1", "1");
        bindKey(inputMap, actionMap, KeyEvent.VK_2, 0, "type-2", "2");
        bindKey(inputMap, actionMap, KeyEvent.VK_3, 0, "type-3", "3");
        bindKey(inputMap, actionMap, KeyEvent.VK_4, 0, "type-4", "4");
        bindKey(inputMap, actionMap, KeyEvent.VK_5, 0, "type-5", "5");
        bindKey(inputMap, actionMap, KeyEvent.VK_6, 0, "type-6", "6");
        bindKey(inputMap, actionMap, KeyEvent.VK_7, 0, "type-7", "7");
        bindKey(inputMap, actionMap, KeyEvent.VK_8, 0, "type-8", "8");
        bindKey(inputMap, actionMap, KeyEvent.VK_9, 0, "type-9", "9");
        bindKey(inputMap, actionMap, KeyEvent.VK_PERIOD, 0, "type-.", ".");
        bindKey(inputMap, actionMap, KeyEvent.VK_MINUS, 0, "type--", "-");
        bindKey(inputMap, actionMap, KeyEvent.VK_SLASH, 0, "type-/", "/");
        bindKey(inputMap, actionMap, KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK, "type-+", "+");
        bindKey(inputMap, actionMap, KeyEvent.VK_8, InputEvent.SHIFT_DOWN_MASK, "type-*", "*");

        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD0, 0, "type-0", "0");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD1, 0, "type-1", "1");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD2, 0, "type-2", "2");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD3, 0, "type-3", "3");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD4, 0, "type-4", "4");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD5, 0, "type-5", "5");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD6, 0, "type-6", "6");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD7, 0, "type-7", "7");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD8, 0, "type-8", "8");
        bindKey(inputMap, actionMap, KeyEvent.VK_NUMPAD9, 0, "type-9", "9");
        bindKey(inputMap, actionMap, KeyEvent.VK_DECIMAL, 0, "type-.", ".");
        bindKey(inputMap, actionMap, KeyEvent.VK_SUBTRACT, 0, "type--", "-");
        bindKey(inputMap, actionMap, KeyEvent.VK_ADD, 0, "type-+", "+");
        bindKey(inputMap, actionMap, KeyEvent.VK_MULTIPLY, 0, "type-*", "*");
        bindKey(inputMap, actionMap, KeyEvent.VK_DIVIDE, 0, "type-/", "/");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "equals");
        actionMap.put("equals", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateResult();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace");
        actionMap.put("backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backspace();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        actionMap.put("clear", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pi");
        actionMap.put("pi", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appendInput("pi");
            }
        });
    }

    private void bindKey(InputMap inputMap, ActionMap actionMap, int keyCode, int modifiers, String actionKey, String value) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleButton(value);
            }
        });
    }

    private double evaluateExpression(String expr) {
        Parser parser = new Parser(expr);
        double value = parser.parseExpression();
        if (parser.hasRemaining()) {
            throw new IllegalArgumentException("Unexpected text at end of expression");
        }
        return value;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private static class Parser {
        private final String input;
        private int position;

        public Parser(String input) {
            this.input = input.replaceAll("\\s+", "");
        }

        public double parseExpression() {
            double value = parseTerm();
            while (true) {
                if (match('+')) {
                    value += parseTerm();
                } else if (match('-')) {
                    value -= parseTerm();
                } else {
                    break;
                }
            }
            return value;
        }

        private double parseTerm() {
            double value = parseFactor();
            while (true) {
                if (match('*')) {
                    value *= parseFactor();
                } else if (match('/')) {
                    double divisor = parseFactor();
                    value /= divisor;
                } else {
                    break;
                }
            }
            return value;
        }

        private double parseFactor() {
            if (match('+')) {
                return parseFactor();
            }
            if (match('-')) {
                return -parseFactor();
            }
            if (match('(')) {
                double value = parseExpression();
                if (!match(')')) {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                }
                return value;
            }
            if (peekStartsWith("pi")) {
                position += 2;
                return Math.PI;
            }
            return parseNumber();
        }

        private double parseNumber() {
            int start = position;
            while (position < input.length() && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
                position++;
            }
            if (start == position) {
                throw new IllegalArgumentException("Number expected at position " + position);
            }
            return Double.parseDouble(input.substring(start, position));
        }

        private boolean match(char expected) {
            if (position < input.length() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private boolean peekStartsWith(String token) {
            return input.regionMatches(position, token, 0, token.length());
        }

        public boolean hasRemaining() {
            return position < input.length();
        }
    }
}

