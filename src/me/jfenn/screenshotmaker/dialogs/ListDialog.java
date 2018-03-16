package me.jfenn.screenshotmaker.dialogs;

import me.jfenn.screenshotmaker.interfaces.Nameable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ListDialog<T extends Nameable> {

    private List<T> list;
    private JList<String> jList;
    private JDialog dialog;
    private ListActionListener<T> listener;

    public ListDialog(Component parent, String title, List<T> list) {
        this.list = list;
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            names[i] = list.get(i).getName();

        jList = new JList<>(names);
        jList.addListSelectionListener(e -> {
            if (listener != null)
                listener.performAction(this, getSelectedItem());
        });

        DefaultListCellRenderer renderer = (DefaultListCellRenderer) jList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            if (listener != null)
                listener.onModified(list);
            hide();
        });

        JButton newItemButton = new JButton("New...");
        newItemButton.addActionListener(e -> {
            if (listener != null)
                listener.performAction(this, null);
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(jList, BorderLayout.CENTER);

        JOptionPane optionPane = new JOptionPane(panel);
        optionPane.setOptions(new JButton[]{okButton, newItemButton});
        dialog = optionPane.createDialog(parent, title);
        dialog.pack();
    }

    public void setActionListener(ListActionListener<T> listener) {
        this.listener = listener;
    }

    public void modify(T originalItem, T newItem) {
        if (newItem != null && originalItem != null)
            list.set(list.indexOf(originalItem), newItem);
        else if (newItem != null) {
            if (list.contains(newItem)) {
                JOptionPane.showMessageDialog(null, "An item already exists with this name.", "Error", JOptionPane.ERROR_MESSAGE);
            } else list.add(newItem);
        } else if (originalItem != null)
            list.remove(originalItem);

        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            names[i] = list.get(i).getName();

        jList.setListData(names);
        dialog.pack();
    }

    public void show() {
        dialog.setVisible(true);
    }

    private void hide() {
        dialog.setVisible(false);
    }

    public T getSelectedItem() {
        String itemName = jList.getSelectedValue();
        for (T item : list) {
            if (item.getName().equals(itemName))
                return item;
        }

        return null;
    }

    public interface ListActionListener<T extends Nameable> {
        void performAction(ListDialog<T> dialog, T item);

        void onModified(List<T> list);
    }

}