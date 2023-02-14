from collections import defaultdict
from abc import ABC, abstractmethod

# cache abstract class
class Cache(ABC):
    def __init__(self, name, size, lineSize, setSize, replacementPolicy) -> None:
        self.name = name
        self.size = size
        self.lineSize = lineSize
        self.setSize = setSize
        self.replacementPolicy = replacementPolicy
        self.hits = 0
        self.misses = 0
        self.entries = defaultdict(LineData) # entries currently in each set
        self.freeEntries = defaultdict(int) # number of free entries in each set

    def __str__(self) -> str:
        return f"Name:\t\t\t {self.name}\nSize:\t\t\t {self.size}\nLine Size:\t\t {self.lineSize}\nSet Size:\t\t {self.setSize}\nReplacement Policy:\t {self.replacementPolicy}"

    # code to convert a memory address into a 64 bit binary int
    def readAddress(memAddress):
        # https://stackoverflow.com/questions/1425493/convert-hex-to-binary [01/02/2023]
        scale = 16
        addressSize = 64
        return bin(int(memAddress, scale)[2:].zfill(addressSize))

    @abstractmethod
    def op(memAddr, memOp, bytes):
        pass


class DirectMapped(Cache):

    def __init__(self, name, size, lineSize) -> None:
        super().__init__(name, size, lineSize, 0, "direct")

    def op(memAddr, memOp, bytes):
        pass

class NWayAssociative(Cache):

    def __init__(self, name, size, lineSize, setSize, replacementPolicy) -> None:
        super().__init__(name, size, lineSize, setSize, replacementPolicy)

    def op(memAddr, memOp, bytes):
        pass

    
class LineData:
    def __init__(self, tag) -> None:
        self.tag = tag
        self.valid = True