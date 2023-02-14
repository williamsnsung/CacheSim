import sys
import json
from CacheSim import *

# creates an associative cache with the appropriate set size and replacement policy, defaulting to round robin if not given
def createAssociativeCache(setSize, config):
    if ("replacement_policy" in config):
        return NWayAssociative(config["name"], config["size"], config["line_size"], setSize, config["replacement_policy"])
    else:
        return NWayAssociative(config["name"], config["size"], config["line_size"], setSize, "rr")

# validates correct length input parameters and that the config file given is a json
if len(sys.argv) == 3 and sys.argv[1].endswith(".json"):
    prog, configPath, tracePath = sys.argv
    caches = []
    # reading in the configs for the caches and storing them in the above cache array
    with open(configPath, 'r') as configFile:
        configJSON = json.load(configFile)
        for config in configJSON["caches"]:
            if config["kind"] == "direct":
                caches.append(DirectMapped(config["name"], config["size"], config["line_size"]))
                print(caches[-1], "\n")
            else:
                if config["kind"] == "full":
                    caches.append(createAssociativeCache(1, config))
                elif config["kind"] == "2way":
                    caches.append(createAssociativeCache(2, config))
                elif config["kind"] == "4way":
                    caches.append(createAssociativeCache(4, config))
                if config["kind"] == "8way":
                    caches.append(createAssociativeCache(8, config))
                print(caches[-1], "\n")

    # reading in and executing each line of the trace file given
    with open(tracePath, 'r') as traceFile:
        while True:
        
            # Get next line from file
            line = traceFile.readline()
            progCount, memAddr, memOp, size = line.split()

            if not line:
                break
            print(memAddr, memOp, size)

else:
    raise ValueError("Malformed input!")