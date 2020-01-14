package de.bonndan.nivio.input;

import com.googlecode.cqengine.IndexedCollection;
import de.bonndan.nivio.input.dto.ItemDescription;
import de.bonndan.nivio.input.dto.LandscapeDescription;
import de.bonndan.nivio.input.dto.RelationDescription;
import de.bonndan.nivio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Examines the labels of an item for parts that point to being an url and could point to targets in the landscape.
 */
public class MagicLabelRelations extends Resolver {

    /**
     * this could be made configurable later
     */
    private static final List<String> URL_PARTS = Arrays.asList("uri", "url", "host");

    private static final List<String> PROVIDER_INDICATORS = Arrays.asList("db", "database", "provider");

    protected MagicLabelRelations(ProcessLog processLog) {
        super(processLog);
    }

    public void process(LandscapeDescription input, LandscapeImpl landscape) {

        Map<ItemDescription, List<LabelMatch>> itemMatches = new HashMap<>();
        input.getItemDescriptions().all().forEach(item -> itemMatches.put(item, getMatches(item)));

        //search for targets in the landscape
        itemMatches.forEach((description, labelMatches) -> {
            labelMatches.forEach(labelMatch -> {
                labelMatch.possibleTargets.forEach(toFind -> {
                    Collection<? extends LandscapeItem> possibleTargets = landscape.getItems().cqnQueryOnIndex(landscape.getItems().selectByIdentifierOrName(toFind));

                    if (possibleTargets.size() != 1) {
                        processLog.debug("Found no target of magic relation from item " + description.getIdentifier() + " using '" + toFind + "'");
                        return;
                    }

                    String source = description.getIdentifier();
                    String target = possibleTargets.iterator().next().getIdentifier();
                    processLog.info("Found a target of magic relation from " + description.getIdentifier()
                            + " to target '" + target + "' using '" + toFind + "'");
                    boolean relationExists = description.getRelations().stream()
                            .anyMatch(r -> hasRelation(source, target, r));
                    boolean isEqual = source.equals(target);
                    if (!relationExists && !isEqual) {
                        RelationDescription relation = new RelationDescription(source, target);
                        //inverse
                        if (isProvider(labelMatch)) {
                            relation = new RelationDescription(target, source);
                            relation.setType(RelationType.PROVIDER);
                        }
                        description.addRelation(relation);
                        return;
                    }

                    processLog.debug("Relation between " + source + " and " + target + " already exists, not adding magic one.");
                });
            });
        });
    }

    private boolean isProvider(LabelMatch labelMatch) {
        List<String> labelParts = Arrays.stream(labelMatch.key.split("_"))
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return labelParts.stream().anyMatch(PROVIDER_INDICATORS::contains);
    }

    private boolean hasRelation(String source, String target, RelationItem<String> r) {
        return r.getSource().equals(source) && r.getTarget().equals(target) ||
                r.getSource().equals(target) && r.getTarget().equals(source);
    }

    private List<LabelMatch> getMatches(ItemDescription itemDescription) {
        return itemDescription.getLabels().entrySet().stream()
                .map(entry -> getPossibleTargetsForLabel(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LabelMatch getPossibleTargetsForLabel(String key, String value) {
        List<String> keyParts = Arrays.stream(key.split("_")).map(String::toLowerCase).collect(Collectors.toList());
        if (URL_PARTS.stream().noneMatch(keyParts::contains)) {
            return null;
        }

        List<String> aliasesToFind = new ArrayList<>(keyParts);
        try {
            URL url = new URL(value);
            aliasesToFind.add(url.getHost());
            aliasesToFind.addAll(Arrays.asList(url.getPath().split("/"))); //add all path parts
        } catch (MalformedURLException ignored) {
            aliasesToFind.addAll(Arrays.asList(value.split(":")));
        }

        return new LabelMatch(key, value, aliasesToFind);
    }

    private static class LabelMatch {
        String key;
        String value;
        private final List<String> possibleTargets;
        RelationType relationType;

        LabelMatch(String key, String value, List<String> possibleTargets) {
            this.key = key;
            this.value = value;
            this.possibleTargets = possibleTargets;
        }
    }
}