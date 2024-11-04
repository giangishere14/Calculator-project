package calculator;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.awt.event.*;
import javax.script.*;

public class Calculator {
    public static void main(String[] args) {
        CalculatorFrame frame = new CalculatorFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}


class CalculatorFrame extends JFrame {
    JMenuBar menuBar;
    JMenu menuMode;
    JCheckBoxMenuItem scienceMode;

    public CalculatorFrame() {
        setTitle("Calculator");

        CalculatorPanel panel = new CalculatorPanel();
        add(panel);

        scienceMode = new JCheckBoxMenuItem("Science mode");
        scienceMode.setFont(new Font("Arial", Font.PLAIN, 16));
        scienceMode.addItemListener(e -> {
            panel.setScienceMode(e.getStateChange() == ItemEvent.SELECTED);
            pack();
            setLocationRelativeTo(null);
        });

        menuMode = new JMenu("Mode");
        menuMode.setFont(new Font("Arial", Font.PLAIN, 16));
        menuMode.add(scienceMode);

        menuBar = new JMenuBar();
        menuBar.add(menuMode);
        setJMenuBar(menuBar);

        pack();
        setLocationRelativeTo(null);
    }
}


class CalculatorPanel extends JPanel {
    private JTextField txInput, txResult;
    private JPanel plScience;
    private HistoryCalculate history = new HistoryCalculate();
    private String ans = "0";
    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

    public CalculatorPanel() {
        setLayout(new BorderLayout());
        
        
        initScreen();
        initButtons();
        try {
            engine.eval("X=0; Y=0; Z=0");
        } catch (ScriptException e) {
            System.out.println("Error initializing variables X, Y, Z");
        }
    }

    private void initScreen() {
        JPanel plScreen = new JPanel(new BorderLayout());

        
        txInput = createTextField();
        txInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    calculate();
                }
            }
        });
        plScreen.add(txInput, BorderLayout.NORTH);
        txResult = createTextField();
        txResult.setEditable(false);
        plScreen.add(txResult, BorderLayout.CENTER);

        add(plScreen, BorderLayout.NORTH);
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(Color.decode("#333333"));
        textField.setForeground(Color.WHITE);
        textField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
        textField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textField.setCaretColor(Color.WHITE);
        return textField;
    }

    private void initButtons() {
        JPanel plBasic = new JPanel(new GridLayout(6, 5, 5, 5));
        ActionListener alInsert = new InsertAction();
        ActionListener alCommand = new CommandAction();

        String[][] basicButtons = {
            {"←", "→", "Off", "↑", "↓"},
            {"(", ")", "+/-", "%", ","},
            {"7", "8", "9", "DEL", "AC"},
            {"4", "5", "6", "+", "-"},
            {"1", "2", "3", "×", "÷"},
            {"0", ".", "E", "Ans", "="}
        };

        for (String[] row : basicButtons) {
            for (String label : row) {
                plBasic.add(new MyButton(label, "", label.matches("\\d") ? alInsert : alCommand));
            }
        }
        add(plBasic, BorderLayout.CENTER);

        // Science panel initialization
        plScience = new JPanel(new GridLayout(6, 5, 5, 5));
        plScience.setVisible(false);
        add(plScience, BorderLayout.WEST);
    }

    private void calculate() {
        String input = txInput.getText();
        if (!input.isEmpty()) {
            history.add(input);
            String converted = convert(input);
            String result = calculateString(converted);
            txResult.setText("= " + result);
            ans = result;
        }
    }

    private String calculateString(String expr) {
        try {
            Object result = engine.eval(expr);
            return String.valueOf(result);
        } catch (ScriptException e) {
            txResult.setText("ERROR");
            return "ERROR";
        }
    }

    private String convert(String expr) {
        String[][] replacements = {
            {"Ans", ans}, {"%", "/100"}, {"×", "*"}, {"÷", "/"},
            {"Rad", "java.lang.Math.toRadians"}, {"Deg", "java.lang.Math.toDegrees"},
            {"3√", "java.lang.Math.cbrt"}, {"√", "java.lang.Math.sqrt"},
            {"sin", "java.lang.Math.sin"}, {"cos", "java.lang.Math.cos"},
            {"tan", "java.lang.Math.tan"}, {"aSin", "java.lang.Math.asin"},
            {"aCos", "java.lang.Math.acos"}, {"aTan", "java.lang.Math.atan"},
            {"Sinh", "java.lang.Math.sinh"}, {"Cosh", "java.lang.Math.cosh"},
            {"Tanh", "java.lang.Math.tanh"}, {"Ln", "java.lang.Math.log"},
            {"Log", "java.lang.Math.log10"}, {"Pow", "java.lang.Math.pow"},
            {"Rand", "java.lang.Math.random()"}, {"℮", "java.lang.Math.E"},
            {"π", "java.lang.Math.PI"}
        };
        
        for (String[] pair : replacements) {
            expr = expr.replace(pair[0], pair[1]);
        }
        return expr;
    }

    public void setScienceMode(boolean on) {
        plScience.setVisible(on);
    }

    
    private class InsertAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            txInput.setText(txInput.getText() + e.getActionCommand());
        }
    }

    private class CommandAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "Off":
                    if (JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn tắt Calculator?") == JOptionPane.OK_OPTION) {
                        System.exit(0);
                    }
                    break;
                case "AC":
                    txInput.setText("");
                    txResult.setText("");
                    break;
                case "DEL":
                    if (txInput.getCaretPosition() > 0) {
                        txInput.setText(txInput.getText().substring(0, txInput.getCaretPosition() - 1) + txInput.getText().substring(txInput.getCaretPosition()));
                    }
                    break;
                case "=":
                    calculate();
                    break;
            }
        }
    }
}

class MyButton extends JButton {
    public MyButton(String label, String tooltip, ActionListener listener) {
        setText(label);
        setToolTipText(tooltip);
        setPreferredSize(new Dimension(75, 75));
        setFont(new Font("Consolas", Font.BOLD, 26));
        setFocusable(false);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        addActionListener(listener);
    }
}


class HistoryCalculate {
    private ArrayList<String> history = new ArrayList<>();
    private int index = -1;

    public void add(String expr) {
        history.add(expr);
        index = history.size() - 1;
    }

    public String getPre() {
        if (index > 0) index--;
        return history.get(index);
    }

    public String getNext() {
        if (index < history.size() - 1) index++;
        return history.get(index);
    }
}
