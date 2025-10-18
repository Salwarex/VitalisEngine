package ru.belisario.engine.client.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ResourcePool {
    private final Map<String, ResourceSet> sets;

    public ResourcePool(){
        sets = new HashMap<>();
    }

    public void add(String key, ResourceSet set){
        if(contains(key)) throw new RuntimeException("В пуле уже содержится данный ключ");
        sets.put(key, set);
    }

    public Optional<ResourceSet> get(String key){
        if(!contains(key)) return Optional.empty();
        return Optional.of(sets.get(key).clone());
    }

    public boolean contains(String key){
        return sets.containsKey(key);
    }

    public Set<String> keySet(){
        return sets.keySet();
    }
}
