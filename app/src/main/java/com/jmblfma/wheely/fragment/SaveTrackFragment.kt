
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.jmblfma.wheely.R
import com.jmblfma.wheely.data.Difficulty
import com.jmblfma.wheely.databinding.FragmentSaveTrackBinding
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.viewmodels.TrackRecordingViewModel
class SaveTrackFragment : DialogFragment() {

    private var _binding: FragmentSaveTrackBinding? = null
    private val binding get() = _binding!!
    val viewModel: TrackRecordingViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSaveTrackBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadedVehicles = viewModel.loadedVehicles.value?.toMutableList() ?: mutableListOf()
        // adds dummy vehicle to allow choosing none
        loadedVehicles.add(Vehicle(vehicleId = 0, name = getString(R.string.no_vehicle_assigned_option)))
        val vehicleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, loadedVehicles)
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicles.adapter = vehicleAdapter

        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Difficulty.values())
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter

        binding.saveButton.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val description = binding.editTextDescription.text.toString()
            val selectedVehicleId = (binding.spinnerVehicles.selectedItem as Vehicle).vehicleId
            val vehicleId = if (selectedVehicleId == 0) null else selectedVehicleId
            val selectedDifficulty = binding.spinnerDifficulty.selectedItem as Difficulty

            viewModel.saveCurrentTrack(
                name,
                description,
                vehicleId,
                selectedDifficulty
            )
            dismiss()
        }
    }
}

/* TEST METHOD
private fun loadVehicles(): List<Vehicle> {
    // Test List
    val testVehicles = listOf(
        Vehicle(name = "Model S", vehicleId = 1),
        Vehicle(name = "Model 3", vehicleId = 2),
        Vehicle(name = "Model X", vehicleId = 3),
        Vehicle(name = "Model Y", vehicleId = 4)
    )
    return testVehicles
}
 */



