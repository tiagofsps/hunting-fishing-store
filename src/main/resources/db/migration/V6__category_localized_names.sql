-- Phase 6 (Step 27): Portuguese category names.
-- Categories keep the canonical name in `name`; `name_pt` is the localized
-- override used by the storefront when Portuguese is selected.
alter table categories
    add column name_pt varchar(100);

-- Backfill the seeded taxonomy so existing databases get translations without
-- waiting for a reseed.
update categories
set name_pt = case slug
    when 'fishing' then 'Pesca'
    when 'hunting' then 'Caça'
    when 'camping' then 'Campismo'
    when 'rods' then 'Caniços'
    when 'reels' then 'Carretos'
    when 'hunting-apparel' then 'Vestuário'
end
where slug in ('fishing', 'hunting', 'camping', 'rods', 'reels', 'hunting-apparel');
