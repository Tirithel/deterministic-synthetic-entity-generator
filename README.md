# deterministic-synthetic-entity-generator

Deterministically generates synthetic data.

### Future Improvements
- [x] More targeted and improved domains (names, locations, ...) [see MarkovNameGenerator](https://github.com/Tw1ddle/MarkovNameGenerator) Solved using similar approach to MarkovNameGenerator. (Thanks for the data). 
  - [ ] Next up is to generic this. Lets solve this problem for more than just String with different kinds of vocabulary. Maybe all vocabulary >:> Double?
- [ ] Better follow through logic
- [ ] Fork join leveraging block offset to guarantee ordering
- [ ] Think about generically solving this problem - what if entities have `N` number of sub entities? Will they all need a deterministic sequence iterator?

### Credits:
@DarioBalinzo - [Markov4s](https://github.com/DarioBalinzo/Markov4s)
