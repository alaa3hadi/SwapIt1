package com.example.swapit1.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.example.swapit1.databinding.SheetFiltersBinding

class FilterBottomSheet(
    private val initial: FilterOptions = FilterOptions(),
    private val locations: List<String>,
    private val categories: List<String>,
    private val onApply: (FilterOptions) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: SheetFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetFiltersBinding.inflate(inflater, container, false)

        // ترتيب مبدئي
        binding.rbNewest.isChecked = (initial.sort == SortOption.NEWEST)
        binding.rbOldest.isChecked = (initial.sort == SortOption.OLDEST)

        // Chips
        makeChips(binding.chipsLocation, listOf("الكل") + locations, initial.location ?: "الكل")
        makeChips(binding.chipsCategory, listOf("الكل") + categories, initial.category ?: "الكل")

        binding.btnApply.setOnClickListener {
            val sort = if (binding.rbNewest.isChecked) SortOption.NEWEST else SortOption.OLDEST

            val loc = binding.chipsLocation.checkedChipId.let { id ->
                binding.chipsLocation.findViewById<Chip>(id)?.text?.toString()
            }.takeUnless { it.isNullOrBlank() || it == "الكل" }

            val cat = binding.chipsCategory.checkedChipId.let { id ->
                binding.chipsCategory.findViewById<Chip>(id)?.text?.toString()
            }.takeUnless { it.isNullOrBlank() || it == "الكل" }

            onApply(FilterOptions(sort = sort, location = loc, category = cat))
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            onApply(FilterOptions())
            dismiss()
        }

        binding.btnClose.setOnClickListener { dismiss() }
        return binding.root
    }

    private fun makeChips(group: com.google.android.material.chip.ChipGroup, items: List<String>, preselect: String?) {
        group.removeAllViews()
        items.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = (label == preselect)
            }
            group.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }
}
