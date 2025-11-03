package ru.belisario.engine.client.resource;

import java.util.Map;

public class UIResourceSet extends ResourceSet{
    public UIResourceSet(
            String key,
            int[] textureIds,
            Map<String, String> animations,
            Map<String, String> arguments,
            double offsetX,
            double offsetY,
            double sizeX,
            double sizeY
            ) {
        super(key, textureIds, animations, arguments);
    }

}
