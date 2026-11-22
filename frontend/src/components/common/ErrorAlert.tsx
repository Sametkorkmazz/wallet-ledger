import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { CircleAlert, RefreshCw } from "lucide-react";

type ErrorAlertProps = {
  title: string;
  description: string;
  isRetrying: boolean;
  onRetry: () => void;
};

export default function ErrorAlert({
  title,
  description,
  onRetry,
  isRetrying,
}: ErrorAlertProps) {
  return (
    <Alert variant="destructive">
      <CircleAlert />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription className="flex flex-col gap-2">
        <p>{description}</p>
        <Button
          className="self-start"
          variant="outline"
          size="sm"
          onClick={() => onRetry()}
          disabled={isRetrying}
        >
          <RefreshCw />
          Try again
        </Button>
      </AlertDescription>
    </Alert>
  );
}
