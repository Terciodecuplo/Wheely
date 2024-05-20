
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
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
        // DISABLED AT UI LEVEL 20240508; user MUST go and create a vehicle before saving the track
        // we prevent the user from launching this dialog if there aren't any vehicles added
        // track logic supports NO vehicle tya hough and can be created after restoring the dummy one:
        // Dummy 'No Vehicle' option to allow choosing none
        // loadedVehicles.add(Vehicle(vehicleId = 0, name = getString(R.string.no_vehicle_assigned_option)))
        val vehicleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, loadedVehicles)
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicles.adapter = vehicleAdapter

        val difficultyPairs = Difficulty.entries.map { it to it.getLocalizedName(requireContext()) }
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,  difficultyPairs.map { it.second })
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter
        // store the map in the tag for later retrieval of the difficulty objects from their name

        binding.saveButton.setOnClickListener {
            // text length is limited in the UI
            val name = binding.editTextName.text.toString()
            val description = binding.editTextDescription.text.toString()
            val selectedVehicleId = (binding.spinnerVehicles.selectedItem as Vehicle).vehicleId
            val vehicleId = if (selectedVehicleId == 0) null else selectedVehicleId
            val selectedDifficultyName = binding.spinnerDifficulty.selectedItem as String
            // 1. first gets the pair Difficulty, Name; 2. first gets the associated Difficulty
            val selectedDifficulty = difficultyPairs.first { it.second == selectedDifficultyName }.first

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



