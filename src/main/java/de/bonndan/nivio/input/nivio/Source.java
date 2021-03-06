package de.bonndan.nivio.input.nivio;

import de.bonndan.nivio.input.dto.GroupDescription;
import de.bonndan.nivio.input.dto.ItemDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents source file content with its sections.
 */
public class Source {

    public List<ItemDescription> items = new ArrayList<>();

    public List<GroupDescription> groups = new ArrayList<>();
}
