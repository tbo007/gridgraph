package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.Collection;

public class PositionAwareList<E extends PositionAwareList.PositionAware> extends ArrayList<E> {
    @Override
    public boolean add(E element) {
        boolean result = super.add(element);
        if (result) {
            element.setPosition(size() - 1);
        }
        return result;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        updatePositions(index);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean result = super.addAll(collection);
        if (result) {
            updatePositions(size() - collection.size());
        }
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        boolean result = super.addAll(index, collection);
        if (result) {
            updatePositions(index);
        }
        return result;
    }

    @Override
    public E set(int index, E element) {
        E result = super.set(index, element);
        element.setPosition(index);
        return result;
    }

    @Override
    public E remove(int index) {
        E result = super.remove(index);
        updatePositions(index);
        return result;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        boolean result = super.remove(o);
        if (result) {
            updatePositions(index);
        }
        return result;
    }

    private void updatePositions(int startIndex) {
        for (int i = startIndex; i < size(); i++) {
            get(i).setPosition(i);
        }
    }

    public interface PositionAware {
        void setPosition(int position);
    }
}
