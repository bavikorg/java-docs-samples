/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package compute.custommachinetype;

// [START compute_custom_machine_type_create]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateCustomMachineType {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-google-cloud-project-id";
    // Name of the zone to create the instance in. For example: "us-west3-b".
    String zone = "gcloud-zone";
    // Name of the new virtual machine (VM) instance.
    String instanceName = "instance-name";
    // Machine type of the VM being created. This value uses the
    // following format: "zones/{zone}/machineTypes/{typeName}".
    // For example: "zones/europe-west3-c/machineTypes/f1-micro"
    // OR
    // It can be a CustomMachineType object, describing a custom type you want to use.
    String machineType = "zones/{zone}/machineTypes/{typeName}";

    createInstanceWithCustomMachineType(projectId, zone, instanceName, machineType);
  }

  // Sends an instance creation request to the Compute Engine API and waits for it to complete.
  public static Instance createInstanceWithCustomMachineType(String project, String zone,
      String instanceName, String machineType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(
              // Describe the size and source image of the boot disk to attach to the instance.
              AttachedDiskInitializeParams.newBuilder()
                  .setSourceImage(
                      String.format("projects/debian-cloud/global/images/family/%s", "debian-11"))
                  .setDiskSizeGb(10)
                  .build()
          )
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.name())
          .build();

      // Collect information into the Instance object.
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(
              NetworkInterface.newBuilder().setName("global/networks/default").build())
          .build();

      // Prepare the request to insert an instance.
      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstanceResource(instance)
          .build();

      // Wait for the operation to complete.
      Operation response = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);
      // Check for errors.
      if (response.hasError()) {
        System.out.println("Instance creation failed ! ! " + response);
        return Instance.getDefaultInstance();
      }
      System.out.printf("Instance created : %s", instanceName);
      System.out.println("Operation Status: " + response.getStatus());
      return instancesClient.get(project, zone, instanceName);
    }
  }
}
// [END compute_custom_machine_type_create]